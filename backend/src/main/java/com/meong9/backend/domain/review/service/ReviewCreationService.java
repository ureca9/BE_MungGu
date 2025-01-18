package com.meong9.backend.domain.review.service;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.review.dto.ReviewRequestDto;
import com.meong9.backend.global.kafka.dto.KafkaVideoDto;
import com.meong9.backend.global.kafka.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewCreationService {

    private final ReviewService reviewService;
    private final OutboxService outboxService;

    // Kafka에 메시지를 보내는 작업이 DB 트랜잭션 경계 밖에서 수행되도록 리팩토링
    public void createReviewWithVideos(ReviewRequestDto dto, Member member) {
        List<KafkaVideoDto> kafkaVideoDtos = reviewService.createReview(dto, member);
        if (!kafkaVideoDtos.isEmpty()) {
            outboxService.saveOutboxMessages(kafkaVideoDtos);
        }
    }
}
