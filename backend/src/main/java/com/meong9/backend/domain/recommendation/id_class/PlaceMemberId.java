package com.meong9.backend.domain.recommendation.id_class;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import lombok.*;

import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@Getter
@Builder
public class PlaceMemberId implements Serializable {
    private Long memberId;
    private Long placeId;
}
