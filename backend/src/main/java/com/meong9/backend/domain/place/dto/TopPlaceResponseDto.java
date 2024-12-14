package com.meong9.backend.domain.place.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class TopPlaceResponseDto {
    private final Long placeId;
    private final String placeName;
    private final Integer reviewCount;
    private final BigDecimal reviewAvg;
    private final String province;
    private final String cityDistrict;
    private final String subDistrict;

    public TopPlaceResponseDto(Long placeId, String placeName, Integer reviewCount, BigDecimal reviewAvg, String province, String cityDistrict, String subDistrict, Long viewCount) {
        this.placeId = placeId;
        this.placeName = placeName;
        this.reviewCount = reviewCount;
        this.reviewAvg = reviewAvg;
        this.province = province;
        this.cityDistrict = cityDistrict;
        this.subDistrict = subDistrict;
        this.viewCount = viewCount;
    }

    private final Long viewCount;

    @Setter
    private String placeImageUrl;

}
