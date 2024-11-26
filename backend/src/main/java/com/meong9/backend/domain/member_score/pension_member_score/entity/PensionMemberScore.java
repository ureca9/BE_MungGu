package com.meong9.backend.domain.member_score.pension_member_score.entity;

import com.meong9.backend.domain.member_score.id_class.PensionMemberId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pension_member_score")
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class PensionMemberScore {
    @EmbeddedId
    private PensionMemberId pensionMemberId;

//    @Column(precision = 10, scale = 4, nullable = false)
//    private BigDecimal score;
    private float score;

    private LocalDateTime lastUpdatedAt;

    public void setScore(float score) {
        this.score = score;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
