package com.meong9.backend.domain.place.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PlaceSummaryResponseDto {
    private final String pensionName;
    @DecimalMin(value = "0.0", message = "리뷰 평균은 0.0 이상이어야 합니다")
    @DecimalMax(value = "5.0", message = "리뷰 평균은 5.0 이하여야 합니다")
    private final Double reviewAvg;
    @Min(value = 0, message = "리뷰 개수는 0 이상이어야 합니다")
    private final Integer reviewCount;
}
