package com.meong9.backend.domain.member_score.id_class;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@Getter
public class PensionMemberId implements Serializable {
    private Long pensionId;
    private Long memberId;
}
