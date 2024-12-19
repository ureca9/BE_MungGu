package com.meong9.backend.global.batch.place.dto;

import com.meong9.backend.domain.place.entity.TopPlace;
import com.meong9.backend.global.topFeature.entity.TopFeature;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopPlaceAndFeature {
    private TopPlace topPlace;
    private TopFeature topFeature;
}
