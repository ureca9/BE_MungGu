package com.meong9.backend.domain.pension.dto;

import com.meong9.backend.domain.review.dto.PhotoReviewSummaryResponseDto;
import com.meong9.backend.domain.review.dto.ReviewSummaryResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
public class PensionDetailResponseDto {
    private final Long pensionId;
    private final String pensionName;
    private final Integer reviewCount;
    private final Double reviewAvg;
    private final String address;
    private final List<String> tags;
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

    private final List<String> images;

    private final List<PhotoReviewSummaryResponseDto> photoReviewList;
    private final List<ReviewSummaryResponseDto> review;
}
