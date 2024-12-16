package com.meong9.backend.domain.recommendation.recommendation.entity;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.pension.entity.Pension;
import com.meong9.backend.domain.recommendation.id_class.PensionMemberId;
import com.meong9.backend.domain.recommendation.member_score.pension_place_score.entity.PensionPlaceScore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PensionRecommendation {
    @EmbeddedId
    private PensionMemberId pensionMemberId;

    private Float score;

    private LocalDateTime lastUpdatedAt;

    @MapsId("memberId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @MapsId("pensionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pension_id")
    private Pension pension;

    @Builder
    public PensionRecommendation(PensionMemberId pensionMemberId, Float score, LocalDateTime lastUpdatedAt) {
        this.pensionMemberId = pensionMemberId;
        this.score = score;
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
