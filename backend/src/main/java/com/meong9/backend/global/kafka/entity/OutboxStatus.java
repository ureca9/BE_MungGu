package com.meong9.backend.global.kafka.entity;

public enum OutboxStatus {
    PENDING,
    SENT,
    FAILED
}
