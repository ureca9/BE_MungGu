package com.meong9.backend.global.batch.place.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CreatePlaceFeatureDto {
    private Long topPlaceId;
    private Long topFeatureId;
}
