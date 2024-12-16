package com.meong9.backend.domain.recommendation.member_score.place_member_score.repository;

import com.meong9.backend.domain.recommendation.member_score.dto.MemberScoreDto;
import com.meong9.backend.domain.recommendation.member_score.place_member_score.entity.PlaceMemberScore;
import com.meong9.backend.domain.recommendation.id_class.PlaceMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PlaceMemberScoreRepository extends JpaRepository<PlaceMemberScore, PlaceMemberId> {
    @Transactional
    long deleteByLastUpdatedAtBefore(LocalDateTime threshold);

    @Query("SELECT pms FROM PlaceMemberScore pms WHERE pms.place.placeId = :placeId")
    List<PlaceMemberScore> findByPlaceId(@Param("placeId") Long placeId);

    @Query("SELECT DISTINCT pms.placeMemberId.placeId FROM PlaceMemberScore pms")
    List<Long> findAllPlaceIds();

    @Query("SELECT pms.score FROM PlaceMemberScore pms WHERE pms.placeMemberId.placeId = :placeId AND pms.placeMemberId.memberId = :memberId")
    float findScoreByPlaceIdAndMemberId(@Param("placeId") Long placeId, @Param("memberId") Long memberId);

    @Query("""
        SELECT pms.member.memberId, AVG(pms.score)
        FROM PlaceMemberScore pms
        WHERE pms.member.memberId IN :memberIds AND pms.place.placeId IN :placeIds
        GROUP BY pms.member.memberId
    """)
    List<MemberScoreDto> findScoresBatch(@Param("memberIds") List<Long> memberIds, @Param("placeIds") List<Long> placeIds);

}
