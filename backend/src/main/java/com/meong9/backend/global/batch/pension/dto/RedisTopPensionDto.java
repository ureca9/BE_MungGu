package com.meong9.backend.global.batch.pension.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedisTopPensionDto {
    private Long pensionId;
    private Double score;
    private Integer rank;

    private String pensionName;
    private Integer reviewCount;
    private Double reviewAvg;
    private Integer likeCount;

    private String province;
    private String cityDistrict;
    private String subDistrict;

    private BigDecimal roomPriceAvg;

    private List<Long> tagIds;
}
