package com.meong9.backend.domain.place.entity;

import com.meong9.backend.domain.place.entity.id.PlaceFeatureId;
import com.meong9.backend.global.topFeature.entity.TopFeature;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "place_feature")
@NoArgsConstructor
@AllArgsConstructor
public class PlaceFeature {
    @EmbeddedId
    private PlaceFeatureId id;

    @ManyToOne
    @MapsId("topPlaceId")
    @JoinColumn(name = "top_place_id")
    private TopPlace topPlace;

    @ManyToOne
    @MapsId("topFeatureId")
    @JoinColumn(name = "top_feature_id")
    private TopFeature topFeature;
}
