package com.meong9.backend.global.slack.service;

import com.meong9.backend.global.config.RateLimiterProvider;
import com.meong9.backend.global.slack.entity.SlackNotificationType;
import com.slack.api.Slack;
import com.slack.api.model.block.Blocks;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.BlockCompositions;
import com.slack.api.webhook.WebhookPayloads;
import io.github.resilience4j.ratelimiter.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class SlackNotificationService {

    private final RateLimiter rateLimiter;

    public SlackNotificationService(RateLimiterProvider rateLimiterProvider) {
        this.rateLimiter = rateLimiterProvider.createSlackRateLimiter();
    }

    @Value("${slack.webhook.url}")
    private String webhookUrl;

    // Slack 알림 전송
    @Retryable(
            retryFor = IOException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000)
            )
    public void sendSlackNotification(SlackNotificationType type, String message) {
        // 레이트 리미터를 사용하여 알림 전송
        Runnable slackNotificationTask = RateLimiter.decorateRunnable(rateLimiter, () -> {
            try {
                // 메시지 내용 생성
                List<LayoutBlock> layoutBlocks = generateLayoutBlock(type, message);

                // Slack API 호출하여 메시지 전송
                Slack.getInstance().send(
                        webhookUrl,
                        WebhookPayloads.payload(p -> p.username(type.getUsername())
                                .iconUrl(type.getIconUrl())
                                .blocks(layoutBlocks))
                );
            } catch (IOException e) {
                log.error("Slack 알림 전송 실패: {}", message, e);
                throw new RuntimeException("Slack 알림 전송 중 오류 발생", e);
            }
        });

        // 알림 실행
        try {
            slackNotificationTask.run();
        } catch (Exception e) {
            log.error("Slack 알림 처리 중 오류 발생: {}", message, e);
            throw e; // 상위 호출자에게 예외 전달
        }
    }

    // LayoutBlock 생성
    private List<LayoutBlock> generateLayoutBlock(SlackNotificationType type, String message) {
        return Blocks.asBlocks(
                getHeader(type.getTitle()),
                Blocks.divider(),
                getSection(message)
        );
    }

    // Slack 메시지의 제목 생성
    private LayoutBlock getHeader(String text) {
        return Blocks.header(h -> h.text(
                BlockCompositions.plainText(pt -> pt.emoji(true).text(text))));
    }

    // Slack 메시지 섹션 생성
    private LayoutBlock getSection(String message) {
        return Blocks.section(s -> s.text(
                BlockCompositions.markdownText(message)));
    }
}

