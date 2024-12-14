package com.meong9.backend.domain.pension.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PensionInfoDto {
    private final Long pensionId;
    private final String pensionName;
    private final Integer reviewCount;
    private final Double reviewAvg;
    private final String startTime;
    private final String endTime;
    private final String telNo;
    private final String latitude;
    private final String longitude;
    private final String description;
    private final String enterPetSize;
    private final String info;
    private final String introduction;
    private final String limitInfo;
    private final Boolean likeStatus;
}
