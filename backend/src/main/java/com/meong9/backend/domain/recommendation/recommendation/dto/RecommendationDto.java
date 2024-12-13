package com.meong9.backend.domain.recommendation.recommendation.dto;

import com.meong9.backend.domain.pension.entity.Pension;
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

    public static RecommendationDto createdRecommendationDto(Pension pension, String address){
        String img = null;

        if(pension.getPensionFiles().size() > 0){
            img = pension.getPensionFiles().get(0).getMediaFile().getFileUrl();
        }

        return RecommendationDto.builder()
                .id(pension.getPensionId())
                .name(pension.getName())
                .address(address)
                .img(img)
                .reviewAvg(pension.getReviewAvg().toString())
                .reviewCount(pension.getReviewCount())
                .build();

    }
}
