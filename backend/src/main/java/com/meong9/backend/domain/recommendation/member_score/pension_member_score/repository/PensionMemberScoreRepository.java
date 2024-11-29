package com.meong9.backend.domain.recommendation.member_score.pension_member_score.repository;

import com.meong9.backend.domain.recommendation.member_score.pension_member_score.entity.PensionMemberScore;
import com.meong9.backend.domain.recommendation.id_class.PensionMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface PensionMemberScoreRepository extends JpaRepository<PensionMemberScore, PensionMemberId> {
    @Transactional
    long deleteByLastUpdatedAtBefore(LocalDateTime threshold);

    @Query("SELECT pms.score FROM PensionMemberScore pms WHERE pms.pensionMemberId.pensionId = :pensionId AND pms.pensionMemberId.memberId = :memberId")
    float findScoreByPensionIdAndMemberId(@Param("pensionId") Long pensionId, @Param("memberId") Long memberId);
}
