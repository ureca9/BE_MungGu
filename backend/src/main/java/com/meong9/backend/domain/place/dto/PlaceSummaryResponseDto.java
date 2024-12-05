package com.meong9.backend.domain.place.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PlaceSummaryResponseDto {
    private final String pensionName;
    private final Double reviewAvg;
    private final Integer reviewCount;
}
