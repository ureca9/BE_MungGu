package com.meong9.backend.global.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class KafkaVideoDto {
    private final Long mediaFileId;
    private final String fileUrl;
}
