package com.meong9.backend.global.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meong9.backend.domain.review.entity.ReviewFile;
import com.meong9.backend.domain.review.repository.ReviewFileRepository;
import com.meong9.backend.global.exception.NotFoundException;
import com.meong9.backend.global.kafka.dto.VideoMessage;
import com.meong9.backend.global.kafka.service.OutboxService;
import com.meong9.backend.global.kafka.service.VideoProcessingService;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import com.meong9.backend.global.mediafile.repository.MediaFileRepository;
import com.meong9.backend.global.slack.entity.SlackNotificationType;
import com.meong9.backend.global.slack.service.SlackNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoProcessingConsumer {

    private final VideoProcessingService videoProcessingService;
    private final MediaFileRepository mediaFileRepository;
    private final ReviewFileRepository reviewFileRepository;
    private final OutboxService outboxService;
    private final SlackNotificationService slackNotificationService;

    @RetryableTopic(
            // default 3번 재시도
            backoff = @Backoff(delay = 5000), // 재시도 간격 (5초)
            autoCreateTopics = "false",
            dltTopicSuffix = ".dlt"  // 토픽 자동 생성 방지
    )
    @KafkaListener(topics = "video-transcoding", groupId = "video-transcoding-group", concurrency = "2")
    public void listen(String message) throws MalformedURLException {
        try {
            VideoMessage videoMessage = parseMessage(message);
            videoProcessingService.processVideo(videoMessage);
            log.info("비디오 처리 완료: {}", videoMessage.getFileUrl());
        } catch (Exception e) {
            log.error("비디오 처리 실패: {}", message, e);
            throw e;
        }
    }

    private VideoMessage parseMessage(String message) {
        try {
            return new ObjectMapper().readValue(message, VideoMessage.class);
        } catch (Exception e) {
            throw new RuntimeException("카프카 메시지 파싱 중 에러 발생: " + message, e);
        }
    }

    @KafkaListener(topics = "video-transcoding.dlt", groupId = "video-transcoding-group")
    public void handleFailedMessage(String message) {
        try {
            // JSON 메시지 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            VideoMessage videoMessage = objectMapper.readValue(message, VideoMessage.class);

            String fileUrl = videoMessage.getFileUrl();

            MediaFile mediaFile = mediaFileRepository.findByFileUrl(fileUrl)
                    .orElseThrow(() -> new NotFoundException("Media file not found: " + fileUrl));
            ReviewFile reviewFile = reviewFileRepository.findByMediaFileId(mediaFile.getMediaFileId())
                    .orElseThrow(() -> new NotFoundException("Review file not found for mediaFileId: " + mediaFile.getMediaFileId()));

            // ReviewFile 상태 업데이트
            reviewFile.setStatus("FAILED");
            reviewFileRepository.save(reviewFile);

            // Outbox 상태 관리
            outboxService.updateOutbox(mediaFile.getMediaFileId());

            // Slack 알림 전송
            String slackMessage = String.format(
                    "트랜스코딩 실패 - MediaFile ID: %d, Review ID: %s, 파일 URL: %s",
                    mediaFile.getMediaFileId(), reviewFile.getId().getReviewId(), fileUrl
            );
            slackNotificationService.sendSlackNotification(SlackNotificationType.KAFKA, slackMessage);

            log.info("DLT 메시지 처리 완료: {}", message);
        } catch (Exception e) {
            log.error("DLT 메시지 처리 중 오류 발생: {}", message, e);
        }
    }

}
