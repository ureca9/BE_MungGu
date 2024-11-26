package com.meong9.backend.domain.member_score.place_member_score.repository;

import com.meong9.backend.domain.member_score.place_member_score.entity.PlaceMemberScore;
import com.meong9.backend.domain.member_score.id_class.PlaceMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface PlaceMemberScoreRepository extends JpaRepository<PlaceMemberScore, PlaceMemberId> {
    @Transactional
    void deleteByLastUpdatedAtBefore(LocalDateTime threshold);
}
