package com.meong9.backend.domain.recommendation.recommendation.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RecommendationDto {
    private Long id;
    private String name;
    private String address;
    private String img;
    private String reviewAvg;
    private Integer reviewCount;
}
