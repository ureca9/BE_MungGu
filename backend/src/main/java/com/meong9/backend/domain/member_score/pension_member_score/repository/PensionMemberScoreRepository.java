package com.meong9.backend.domain.member_score.pension_member_score.repository;

import com.meong9.backend.domain.member_score.pension_member_score.entity.PensionMemberScore;
import com.meong9.backend.domain.member_score.id_class.PensionMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface PensionMemberScoreRepository extends JpaRepository<PensionMemberScore, PensionMemberId> {
    @Transactional
    void deleteByLastUpdatedAtBefore(LocalDateTime threshold);
}
