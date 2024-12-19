package com.meong9.backend.domain.pension.dto;

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
public class TopPensionResponseDto {
    private Long pensionId;
    private String pensionName;
    private Integer reviewCount;
    private BigDecimal reviewAvg;
    private String province;
    private String cityDistrict;
    private String subDistrict;
    private Long viewCount;
    private String address;

    private String pensionImageUrl;

    public TopPensionResponseDto(Long pensionId, String pensionName, Integer reviewCount, BigDecimal reviewAvg, String province, String cityDistrict, String subDistrict, Long viewCount) {
        this.pensionId = pensionId;
        this.pensionName = pensionName;
        this.reviewCount = reviewCount;
        this.reviewAvg = reviewAvg;
        this.province = province;
        this.cityDistrict = cityDistrict;
        this.subDistrict = subDistrict;
        this.viewCount = viewCount;
        this.address = AddressMapper.formatAddress(province, cityDistrict, subDistrict);
    }
}
