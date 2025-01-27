package com.meong9.backend.global.kafka.entity;

import com.meong9.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Outbox extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 관련 데이터의 ID (ex. MEDIAFILEID)
    @Column(name = "aggregate_id", nullable = false)
    private Long relatedId;

    // 관련 데이터의 타입 (ex. REVIEW)
    private String relatedType;

    // 이벤트 타입 (ex. TRANSCODE)
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    // Kafka 메시지로 보낼 데이터 (JSON 포맷 권장)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Setter
    private OutboxStatus status;

    @Setter
    private int retryCount;

    @Builder
    public Outbox(Long relatedId, String relatedType, EventType eventType, String payload, OutboxStatus status) {
        this.relatedId = relatedId;
        this.relatedType = relatedType;
        this.eventType = eventType;
        this.payload = payload;
        this.status = status;
        this.retryCount = 0;
    }

}
