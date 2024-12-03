package com.meong9.backend.domain.map.dto;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MapLikeResponseDto {
    private Integer categoryId; // 카테고리 아이디
    private String categoryName;
    private List<MapLikePlaceDto> places;

}
