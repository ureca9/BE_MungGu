package com.meong9.backend.global.batch.place.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RedisTopPlaceDto {
    private Long placeId;
    private Double score;
    private Integer rank;

    private String placeName;
    private Integer reviewCount;
    private Double reviewAvg;
    private Integer likeCount;

    private String province;
    private String cityDistrict;
    private String subDistrict;

    private List<Long> tagIds;
    private String category; // 추가된 필드

}
