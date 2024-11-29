package com.meong9.backend.domain.place.entity.id;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PlaceTagKey implements Serializable {

    private Long placeId; // Place 엔티티의 ID

    private Long tagId; // Tag 엔티티의 ID
}

