package com.meong9.backend.domain.recommendation.member_score.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class MemberScoreDto {
    private Long memberId;
    private Double averageScore;
}
