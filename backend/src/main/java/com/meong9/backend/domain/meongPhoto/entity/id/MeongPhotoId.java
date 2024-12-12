package com.meong9.backend.domain.meongPhoto.entity.id;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class MeongPhotoId implements Serializable {
    private Long memberId;
    private Long mediaFileId;
}
