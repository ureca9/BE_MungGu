package com.meong9.backend.domain.map.dto;

import com.meong9.backend.domain.like.entity.PensionLike;
import com.meong9.backend.domain.like.entity.PlaceLike;
import com.meong9.backend.global.utils.DistanceMapper;
import com.meong9.backend.global.utils.PlaceCodeMapper;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MapLikePlaceDto {
    private Long placeId;
    private String name;
    private String categoryName;
    private Double distance;
    private String address;
    private String businessHour; // 펜션은 없음
    private String latitude;
    private String longitude;
    private List<String> images; // 이미지 최대 3장
    private Boolean isLike;
    private String type;

    // 펜션을 MapLikePlaceDto로
    public static MapLikePlaceDto getPensionToLikePlaceDto(PensionLike pensionLike, String latitude, String longitude, String address, Double distance, List<String> image){

        return MapLikePlaceDto.builder()
                .placeId(pensionLike.getPension().getPensionId())
                .name(pensionLike.getPension().getName())
                .categoryName("펜션")
                .businessHour(null) // 펜션은 운영시간 없음
                .distance(distance)
                .latitude(latitude)
                .longitude(longitude)
                .images(image)
                .address(address)
                .isLike(true)
                .type(PlaceCodeMapper.getType("020"))
                .build();
    }

    // place를 MapLikePlaceDto로
    public static MapLikePlaceDto getPlaceToLikePlaceDto(PlaceLike placeLike, String latitude, String longitude, String address, Double distance, List<String> image) {

        return MapLikePlaceDto.builder()
                .placeId(placeLike.getPlace().getPlaceId())
                .name(placeLike.getPlace().getName())
                .categoryName(placeLike.getPlace().getPlcCategory().getName())
                .businessHour(placeLike.getPlace().getBusinessHour())
                .distance(distance)
                .latitude(latitude)
                .longitude(longitude)
                .images(image)
                .address(address)
                .isLike(true)
                .type(PlaceCodeMapper.getType("010"))
                .build();
    }

}
