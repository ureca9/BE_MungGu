package com.meong9.backend.domain.pension.entity.id;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PensionFeatureId implements Serializable {
    private Long topFeatureId;
    private Long topPensionId;
}
