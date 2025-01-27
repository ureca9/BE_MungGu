package com.meong9.backend.global.kafka.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meong9.backend.global.exception.NotFoundException;
import com.meong9.backend.global.kafka.dto.KafkaVideoDto;
import com.meong9.backend.global.kafka.dto.VideoMessage;
import com.meong9.backend.global.kafka.entity.EventType;
import com.meong9.backend.global.kafka.entity.Outbox;
import com.meong9.backend.global.kafka.entity.OutboxStatus;
import com.meong9.backend.global.kafka.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    public void saveOutboxMessages(List<KafkaVideoDto> kafkaVideoDtos) {
        for (KafkaVideoDto dto : kafkaVideoDtos) {
            try {
                String videoId = UUID.randomUUID().toString();
                String payload = new ObjectMapper().writeValueAsString(new VideoMessage(videoId, dto.getFileUrl()));

                Outbox outbox = Outbox.builder()
                        .relatedId(dto.getMediaFileId())
                        .relatedType("REVIEW_VIDEO")
                        .eventType(EventType.TRANSCODE)
                        .payload(payload)
                        .status(OutboxStatus.PENDING)
                        .build();
                outboxRepository.save(outbox);
                registerTransactionSync(outbox);
            } catch (JsonProcessingException e) {
                log.error("Kafka json 파싱 오류 : {}", e.getMessage());
            }
        }
    }

    private void registerTransactionSync(Outbox outbox) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    kafkaTemplate.send("video-transcoding", outbox.getPayload());
                    updateOutboxStatus(outbox, OutboxStatus.SENT);
                    log.info("Kafka 메시지 전송 성공: {}", outbox.getPayload());
                } catch (Exception e) {
                    log.error("Kafka 메시지 전송 실패: {}", outbox.getPayload(), e);
                    updateOutboxStatus(outbox, OutboxStatus.FAILED);
                }
            }
        });
    }

    private void updateOutboxStatus(Outbox outbox, OutboxStatus status) {
        outbox.setStatus(status);
        outboxRepository.save(outbox);
    }

    public Outbox getOutboxFromMediaFileId(Long mediaFileId) {
        return outboxRepository.findByRelatedIdAndEventType(mediaFileId, EventType.TRANSCODE)
                .orElseThrow(() -> NotFoundException.entityNotFound("Outbox media file id : " + mediaFileId));
    }


    public void updateOutbox(Long mediaFileId) {
        Outbox outbox = getOutboxFromMediaFileId(mediaFileId);
        outbox.setStatus(OutboxStatus.FAILED);
        outbox.setRetryCount(outbox.getRetryCount() + 1);
        outboxRepository.save(outbox);
    }
}
