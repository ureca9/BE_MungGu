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
@NoArgsConstructor
@AllArgsConstructor
public class PensionDetailResponseDto {
    private Long pensionId;
    private String pensionName;
    private Integer reviewCount;
    private Double reviewAvg;
    private String address;
    private List<String> tags;
    private String startTime;
    private String endTime;
    private String telNo;
    private String latitude;
    private String longitude;
    private String description;
    private String enterPetSize;
    private String info;
    private String introduction;
    private String limitInfo;

    private List<String> images;

    private List<PhotoReviewSummaryResponseDto> photoReviewList;
    private List<ReviewSummaryResponseDto> review;
}
