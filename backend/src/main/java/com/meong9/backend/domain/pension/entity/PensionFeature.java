package com.meong9.backend.domain.pension.entity;

import com.meong9.backend.domain.pension.entity.id.PensionFeatureId;
import com.meong9.backend.global.topFeature.entity.TopFeature;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pension_feature")
@NoArgsConstructor
@AllArgsConstructor
public class PensionFeature {
    @EmbeddedId
    private PensionFeatureId id;

    @ManyToOne
    @MapsId("topFeatureId") // 복합 키의 topFeatureId와 매핑
    @JoinColumn(name = "top_feature_id")
    private TopFeature topFeature;

    @ManyToOne
    @MapsId("topPensionId") // 복합 키의 topPensionId와 매핑
    @JoinColumn(name = "top_pension_id")
    private TopPension topPension;
}

