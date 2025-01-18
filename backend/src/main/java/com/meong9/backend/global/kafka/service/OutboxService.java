package com.meong9.backend.global.kafka.service;

import com.meong9.backend.global.exception.NotFoundException;
import com.meong9.backend.global.kafka.dto.KafkaVideoDto;
import com.meong9.backend.global.kafka.entity.EventType;
import com.meong9.backend.global.kafka.entity.Outbox;
import com.meong9.backend.global.kafka.entity.OutboxStatus;
import com.meong9.backend.global.kafka.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void saveOutboxMessages(List<KafkaVideoDto> kafkaVideoDtos) {
        for (KafkaVideoDto dto : kafkaVideoDtos) {
            Outbox outbox = Outbox.builder()
                    .relatedId(dto.getMediaFileId())
                    .relatedType("REVIEW_VIDEO")
                    .eventType(EventType.TRANSCODE)
                    .payload(dto.getFileUrl())
                    .status(OutboxStatus.PENDING)
                    .build();
            outboxRepository.save(outbox);
            registerTransactionSync(outbox);
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
