package com.meong9.backend.domain.search.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PuppiesWithWeightDto {
    private final Long puppyId;
    private final String puppyName;
    private final Double puppyWeight;
    private final String puppyImageUrl;

    @Builder
    public PuppiesWithWeightDto(Long puppyId, String puppyName, Double puppyWeight, String puppyImageUrl) {
        this.puppyId = puppyId;
        this.puppyName = puppyName;
        this.puppyWeight = puppyWeight;
        this.puppyImageUrl = puppyImageUrl;
    }
}
