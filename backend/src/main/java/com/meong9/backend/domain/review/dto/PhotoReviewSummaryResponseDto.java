package com.meong9.backend.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
public class PhotoReviewSummaryResponseDto {
    private Long reviewId;
    private String representativeImageUrl;
    private Long photoReviewCount;


    public PhotoReviewSummaryResponseDto(Long reviewId, String representativeImageUrl, Long photoReviewCount) {
        this.reviewId = reviewId;
        this.representativeImageUrl = representativeImageUrl;
        this.photoReviewCount = photoReviewCount;
    }
}