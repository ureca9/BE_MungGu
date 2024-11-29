package com.meong9.backend.domain.recommendation.id_class;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@Getter
@ToString
public class PensionPlaceId implements Serializable {
    private Long pensionId;
    private Long placeId;
}
