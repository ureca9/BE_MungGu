package com.meong9.backend.domain.pension.entity.id;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class PensionTagId implements Serializable {
    private Long pensionId;
    private Long tagId;
}
