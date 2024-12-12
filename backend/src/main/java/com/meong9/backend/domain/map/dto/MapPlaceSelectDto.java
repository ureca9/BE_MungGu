package com.meong9.backend.domain.map.dto;

import lombok.*;

import java.util.List;


@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MapPlaceSelectDto {
    private Long id; // 펜션 or 시설 아이디
    private String type; // 펜션 or 시설
    private String name;
    private Double distance; // 미터 단위
    private String address;
    private String businessHour; // 펜션은 없음
    private List<String> images; // 대표 이미지
    private String latitude;
    private String longitude;
    private Boolean isLike;

    public static MapPlaceSelectDto createMapPlaceDto(Long id, String type, String name, String latitude, String longitude,
                                                      List<String> images, Double distance, String address, String businessHour, boolean isLike)
    {
        return MapPlaceSelectDto.builder()
                .id(id)
                .type(type)
                .name(name)
                .distance(distance)
                .address(address)
                .businessHour(businessHour)
                .images(images)
                .latitude(latitude)
                .longitude(longitude)
                .isLike(isLike)
                .build();
    }

}
