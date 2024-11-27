package com.meong9.backend.domain.recommendation.member_score.place_member_score.entity;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.recommendation.id_class.PlaceMemberId;
import com.meong9.backend.domain.place.entity.Place;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "place_member_score")
@Getter
public class PlaceMemberScore {

    @EmbeddedId
    private PlaceMemberId placeMemberId;

    private float score;

    private LocalDateTime lastUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("placeId")
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    public void setScore(float score) {
        this.score = score;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    @Builder
    public PlaceMemberScore(PlaceMemberId placeMemberId, float score, LocalDateTime lastUpdatedAt) {
        this.placeMemberId = placeMemberId;
        this.score = score;
        this.lastUpdatedAt = lastUpdatedAt;
    }
}
