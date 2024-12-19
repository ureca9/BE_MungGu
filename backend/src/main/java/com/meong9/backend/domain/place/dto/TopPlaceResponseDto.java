package com.meong9.backend.domain.place.dto;

import com.meong9.backend.global.utils.AddressMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopPlaceResponseDto {
    private Long placeId;
    private String placeName;
    private Integer reviewCount;
    private BigDecimal reviewAvg;
    private String province;
    private String cityDistrict;
    private String subDistrict;
    private Long viewCount;
    private String address;

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
