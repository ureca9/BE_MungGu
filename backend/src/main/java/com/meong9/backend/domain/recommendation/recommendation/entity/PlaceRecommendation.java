package com.meong9.backend.domain.recommendation.recommendation.entity;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.pension.entity.Pension;
import com.meong9.backend.domain.place.entity.Place;
import com.meong9.backend.domain.recommendation.id_class.PensionPlaceId;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PlaceRecommendation {
    @EmbeddedId
    private PensionPlaceId pensionPlaceId;

    private float score;

    private LocalDateTime lastUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pension_id")
    @MapsId("pensionId")
    private Pension pension;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    @MapsId("placeId")
    private Place place;

    @Builder
    public PlaceRecommendation(PensionPlaceId pensionPlaceId, float score, LocalDateTime lastUpdatedAt) {
        this.pensionPlaceId = pensionPlaceId;
        this.score = score;
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
