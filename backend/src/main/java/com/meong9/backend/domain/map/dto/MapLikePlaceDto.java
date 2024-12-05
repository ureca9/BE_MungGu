package com.meong9.backend.domain.map.dto;

import com.meong9.backend.domain.like.entity.PensionLike;
import com.meong9.backend.domain.like.entity.PlaceLike;
import com.meong9.backend.global.utils.DistanceMapper;
import lombok.*;

import java.util.List;
import java.util.Map;

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

    // 펜션을 MapLikePlaceDto로
    public static MapLikePlaceDto getPensionToLikePlaceDto(PensionLike pensionLike, String latitude, String longitude, String address, Double distance, List<String> image){

        return MapLikePlaceDto.builder()
                .placeId(pensionLike.getPension().getPensionId())
                .placeName(pensionLike.getPension().getName())
                .businessHour(null) // 펜션은 운영시간 없음
                .distance(distance)
                .latitude(latitude)
                .longitude(longitude)
                .images(image)
                .address(address)
                .isLike(true)
                .build();
    }

    // place를 MapLikePlaceDto로
    public static MapLikePlaceDto getPlaceToLikePlaceDto(PlaceLike placeLike, String latitude, String longitude, String address, Double distance, List<String> image) {

        return MapLikePlaceDto.builder()
                .placeId(placeLike.getPlace().getPlaceId())
                .placeName(placeLike.getPlace().getName())
                .businessHour(placeLike.getPlace().getBusinessHour())
                .distance(distance)
                .latitude(latitude)
                .longitude(longitude)
                .images(image)
                .address(address)
                .isLike(true)
                .build();
    }

}
