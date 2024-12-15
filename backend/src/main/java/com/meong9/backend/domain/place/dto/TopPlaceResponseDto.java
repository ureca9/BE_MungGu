package com.meong9.backend.domain.place.dto;

import com.meong9.backend.global.utils.AddressMapper;
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
    private final Long viewCount;
    private final String address;

    @Setter
    private String placeImageUrl;

    public TopPlaceResponseDto(Long placeId, String placeName, Integer reviewCount, BigDecimal reviewAvg, String province, String cityDistrict, String subDistrict, Long viewCount) {
        this.placeId = placeId;
        this.placeName = placeName;
        this.reviewCount = reviewCount;
        this.reviewAvg = reviewAvg;
        this.province = province;
        this.cityDistrict = cityDistrict;
        this.subDistrict = subDistrict;
        this.viewCount = viewCount;
        this.address = AddressMapper.formatAddress(province, cityDistrict, subDistrict);
    }




}
