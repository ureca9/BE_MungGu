package com.meong9.backend.domain.recommendation.member_score.pension_member_score.entity;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.recommendation.id_class.PensionMemberId;
import com.meong9.backend.domain.pension.entity.Pension;
import jakarta.persistence.*;
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

    private float score;

    private LocalDateTime lastUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("pensionId")
    @JoinColumn(name = "pension_id", nullable = false)
    private Pension pension;

    public void setScore(float score) {
        this.score = score;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
