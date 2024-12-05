package com.meong9.backend.domain.pension.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PensionSummaryResponseDto {
    private final String pensionName;
    private final Double reviewAvg;
    private final Integer reviewCount;
}
