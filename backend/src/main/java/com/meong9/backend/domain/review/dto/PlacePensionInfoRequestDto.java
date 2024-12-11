package com.meong9.backend.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlacePensionInfoRequestDto {
    private Long plcPenId;
    private String type;
}
