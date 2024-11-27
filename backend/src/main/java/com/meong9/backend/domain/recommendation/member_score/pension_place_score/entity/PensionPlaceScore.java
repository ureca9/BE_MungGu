package com.meong9.backend.domain.recommendation.member_score.pension_place_score.entity;

import com.meong9.backend.domain.recommendation.id_class.PensionPlaceId;
import com.meong9.backend.domain.pension.entity.Pension;
import com.meong9.backend.domain.place.entity.Place;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@Getter
@Builder
public class PensionPlaceScore {
    @EmbeddedId
    private PensionPlaceId pensionPlaceId;

    @Setter
    private float score;

    @Setter
    private LocalDateTime lastUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("pensionId")
    @JoinColumn(name = "pension_id", nullable = false)
    private Pension pension;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("placeId")
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

}
