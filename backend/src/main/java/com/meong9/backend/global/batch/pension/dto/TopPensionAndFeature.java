package com.meong9.backend.global.batch.pension.dto;

import com.meong9.backend.domain.pension.entity.TopPension;
import com.meong9.backend.global.topFeature.entity.TopFeature;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopPensionAndFeature {
    private TopPension topPension;
    private TopFeature topFeature;
}
