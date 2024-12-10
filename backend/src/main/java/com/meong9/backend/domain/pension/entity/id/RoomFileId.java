package com.meong9.backend.domain.pension.entity.id;


import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class RoomFileId {
    private Long roomId;
    private Long mediaFileId;
}
