package com.meong9.backend.global.slack.service;

import com.meong9.backend.global.slack.entity.SlackNotificationType;
import com.slack.api.Slack;
import com.slack.api.model.block.Blocks;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.composition.BlockCompositions;
import com.slack.api.webhook.WebhookPayloads;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlackNotificationService {

    @Value("${slack.webhook.url}")
    private String webhookUrl;

    private final StringBuilder sb = new StringBuilder();

    // Slack 알림 전송
    public void sendSlackNotification(SlackNotificationType type, String message) {
        try {
            // 메시지 내용 생성
            List<LayoutBlock> layoutBlocks = generateLayoutBlock(type, message);

            // Slack API 호출하여 메시지 전송
            Slack.getInstance().send(webhookUrl, WebhookPayloads
                    .payload(p -> p.username(type.getUsername())
                            .iconUrl(type.getIconUrl())
                            .blocks(layoutBlocks)));
            log.info("Slack 알림 전송 성공: {}", message);
        } catch (IOException e) {
            log.error("Slack 알림 전송 실패: {}", e.getMessage(), e);
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

