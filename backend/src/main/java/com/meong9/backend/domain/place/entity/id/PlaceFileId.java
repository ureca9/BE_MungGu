package com.meong9.backend.domain.place.entity.id;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PlaceFileId implements Serializable {

    private Long placeId; // Place의 ID
    private Long mediaFileId;  // MediaFile의 ID
}
