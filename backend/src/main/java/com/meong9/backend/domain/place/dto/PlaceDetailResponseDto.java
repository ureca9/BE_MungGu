package com.meong9.backend.domain.place.dto;

import com.meong9.backend.domain.review.dto.PhotoReviewSummaryResponseDto;
import com.meong9.backend.domain.review.dto.ReviewRequestDto;
import com.meong9.backend.domain.review.dto.ReviewSummaryResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
public class PlaceDetailResponseDto {
    private final Long placeId;
    private final String placeName;
    private final String category;
    private final Integer reviewCount;
    private final Double reviewAvg;
    private final String address;
    private final List<String> tags;
    private final String businessHour;
    private final String telNo;
    private final String hmpgUrl;
    private final String latitude;
    private final String longitude;
    private final String closedDays;
    private final String price;
    private final String limitInfo;
    private final String description;
    private final String enterPetSize;

    private final List<String> images;

    private final List<PhotoReviewSummaryResponseDto> photoReviewList;
    private final List<ReviewSummaryResponseDto> review;
}


