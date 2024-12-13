package com.meong9.backend.domain.place.entity.id;

import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
@Getter
public class PlaceFeatureId implements Serializable {
    private Long topPlaceId;
    private Long topFeatureId;

    public PlaceFeatureId() {}

    public PlaceFeatureId(Long topPlaceId, Long topFeatureId) {
        this.topPlaceId = topPlaceId;
        this.topFeatureId = topFeatureId;
    }
}
