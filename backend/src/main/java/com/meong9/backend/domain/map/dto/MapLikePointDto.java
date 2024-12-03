package com.meong9.backend.domain.map.dto;

import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MapLikePointDto {
    private Long id; // 펜션, 시설 아이디
    private String type; // 펜션, 시설 타입
    private String name; // 이름
    private String latitude; // 위도
    private String longitude; // 경도
}
