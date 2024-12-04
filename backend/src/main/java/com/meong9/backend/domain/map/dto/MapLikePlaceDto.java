package com.meong9.backend.domain.map.dto;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MapLikePlaceDto {
    private Long placeId;
    private String placeName;
    private Double distance;
    private String address;
    private String businessHour; // 펜션은 없음
    private String latitude;
    private String longitude;
    private List<String> images; // 이미지 최대 3장
    private Boolean isLike;
}
