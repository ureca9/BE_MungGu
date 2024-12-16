package com.meong9.backend.domain.recommendation.recommendation.dto;

import com.meong9.backend.domain.recommendation.id_class.PensionPlaceId;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PensionPlaceScoreDto {
    private PensionPlaceId pensionPlaceId;
    private float score;
    private LocalDateTime lastUpdatedAt;
}
