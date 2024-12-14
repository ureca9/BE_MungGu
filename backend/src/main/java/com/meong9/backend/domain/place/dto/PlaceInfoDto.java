package com.meong9.backend.domain.place.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PlaceInfoDto {
    private final Long placeId;
    private final String placeName;
    private final String category;
    private final Integer reviewCount;
    private final Double reviewAvg;
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
    private final Boolean likeStatus;
}
