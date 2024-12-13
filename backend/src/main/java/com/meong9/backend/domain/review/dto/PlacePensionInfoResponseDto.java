package com.meong9.backend.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class PlacePensionInfoResponseDto {

    @AllArgsConstructor
    @Getter @Setter
    public static class PensionResponse{
        private final String name;
        private final String address;
        private final Double reviewAvg;
        private final Integer reviewCount;
        private final String fileUrl;
        private final Integer likeCount;
    }


    @AllArgsConstructor
    @Getter @Setter
    public static class PlaceResponse{
        private final String name;
        private final String address;
        private final Double reviewAvg;
        private final Integer reviewCount;
        private final String fileUrl;
        private final Integer likeCount;
    }
}
