package com.meong9.backend.domain.review.dto;

import lombok.*;

import java.util.List;

/**
 * 리뷰 요약 정보를 전달하기 위한 DTO 클래스.
 */
@Getter
@Builder
public class ReviewSummaryResponseDto {
    private final Long reviewId;
    private final String profileImageUrl;
    private final String content;
    private final Double score;
    private final String visitDate;
    private final String createdAt;
    private final String modifiedAt;
    private final String nickname;

    @Setter
    private List<ReviewSummaryFileDto> file;



}
