package com.meong9.backend.domain.search.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class SearchPlaceDto {
    private final Long placeId;
    private final String placeName;
    @Setter
    private String address;
    private final String placeType;
    @Setter
    private List<String> tags;
    private final Double reviewAvg;
    private final Integer reviewCount;
    @Setter
    private String weightLimit;
    private final String businessHour;
    @Setter
    private List<String> images;
    @Setter
    private Boolean LikeStatus;

    @Builder
    public SearchPlaceDto(Long placeId, String placeName, String address, String placeType, List<String> tags, Double reviewAvg, Integer reviewCount, String weightLimit, String businessHour, List<String> images, Boolean likeStatus) {
        this.placeId = placeId;
        this.placeName = placeName;
        this.address = address;
        this.placeType = placeType;
        this.tags = tags;
        this.reviewAvg = reviewAvg;
        this.reviewCount = reviewCount;
        this.weightLimit = weightLimit;
        this.businessHour = businessHour;
        this.images = images;
        LikeStatus = likeStatus;
    }

}
