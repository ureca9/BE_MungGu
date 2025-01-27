package com.meong9.backend.global.kafka.repository;

import com.meong9.backend.global.kafka.entity.EventType;
import com.meong9.backend.global.kafka.entity.Outbox;
import com.meong9.backend.global.kafka.entity.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OutboxRepository extends JpaRepository<Outbox, Long> {
    List<Outbox> findByStatus(OutboxStatus outboxStatus);

    Optional<Outbox> findByRelatedIdAndEventType(Long relatedId, EventType eventType);
}
