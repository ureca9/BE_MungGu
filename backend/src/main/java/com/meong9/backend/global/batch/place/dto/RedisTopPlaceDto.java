package com.meong9.backend.global.batch.place.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@EqualsAndHashCode
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
