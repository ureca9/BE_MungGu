package com.meong9.backend.domain.search.dto;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SearchPlaceDto {
    private Long placeId;
    private String placeName;
    private String address;
    private String placeType;
    @Setter
    private List<String> tags;
    private Double reviewAvg;
    private Integer reviewCount;
    private String weightLimit;
    private String businessHour;
    @Setter
    private List<String> images;
    private Boolean likeStatus;

}
