package com.meong9.backend.global.kafka.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class KafkaService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendVideoToKafka(String fileUrl) {
        // 1. video Id를 일단 Uuid로 만들어 보고 이후에 저장 및 정렬이 많이 필요한 경우 다른 id 형태를 고려해 봐야 함
        String videoId = UUID.randomUUID().toString();

        // 2. 여기서 만든 메시지는 "video-transcoding" 토픽으로 전송되어 Consumer가 처리함
        String message = String.format("{\"videoId\":\"%s\", \"fileUrl\":\"%s\"}", videoId, fileUrl);

        kafkaTemplate.send("video-transcoding", message);
        System.out.println("Sent video processing request to Kafka: " + message);
    }
}
