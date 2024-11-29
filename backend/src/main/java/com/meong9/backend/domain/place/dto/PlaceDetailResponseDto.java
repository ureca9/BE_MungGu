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
@NoArgsConstructor
@AllArgsConstructor
public class PlaceDetailResponseDto {
    private Long placeId;
    private String placeName;
    private String category;
    private Integer reviewCount;
    private Double reviewAvg;
    private String address;
    private List<String> tags;
    private String businessHour;
    private String telNo;
    private String hmpgUrl;
    private String latitude;
    private String longitude;
    private String closedDays;
    private String price;
    private String limitInfo;
    private String description;
    private String enterPetSize;
    private List<String> images;

    private List<PhotoReviewSummaryResponseDto> photoReviewList;
    private List<ReviewSummaryResponseDto> review;
}


