package com.meong9.backend.global.slack.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SlackNotificationType {
    GENERAL("Exception detected 🚨", "일반 예외 알림", "https://example.com/general-icon.png"),
    KAFKA("Kafka Alert 🚧", "Kafka 알림", "https://example.com/kafka-icon.png"),
    DLQ("DLQ Alert 🚨", "DLQ 알림", "https://example.com/dlq-icon.png");

    private final String username;
    private final String title;
    private final String iconUrl;
}
