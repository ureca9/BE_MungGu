package com.meong9.backend.domain.review.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlacePensionInfoRequestDto {
    @NotNull(message = "plcPenId는 필수 입력값입니다.")
    private Long plcPenId;
    @NotNull(message = "type 필수 입력값입니다.")
    private String type;
}
