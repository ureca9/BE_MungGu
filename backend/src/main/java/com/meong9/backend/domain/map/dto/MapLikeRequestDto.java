package com.meong9.backend.domain.map.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MapLikeRequestDto {
    private String categoryName; // 카테고리 이름 -> 전체, 카페, 펜션, 마당, 공원, 놀이터, 섬, 해수욕장
    private Double latitude; // 위도
    private Double longitude; // 경도
}
