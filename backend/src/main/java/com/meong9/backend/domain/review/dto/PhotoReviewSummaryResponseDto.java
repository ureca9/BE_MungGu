package com.meong9.backend.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoReviewSummaryResponseDto {
    private Long reviewId;
    private String representativeImageUrl;
    private Integer photoReviewCount;
}