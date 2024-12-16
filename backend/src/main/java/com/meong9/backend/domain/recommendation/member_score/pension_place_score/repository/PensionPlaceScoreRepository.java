package com.meong9.backend.domain.recommendation.member_score.pension_place_score.repository;

import com.meong9.backend.domain.recommendation.id_class.PensionPlaceId;
import com.meong9.backend.domain.recommendation.member_score.pension_place_score.entity.PensionPlaceScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PensionPlaceScoreRepository extends JpaRepository<PensionPlaceScore, PensionPlaceId> {
    @Query("SELECT p.place.placeId FROM PensionPlaceScore p WHERE p.pension.pensionId = :pensionId")
    List<Long> findPlaceIdsByPensionId(Long pensionId);


    @Query("""
        SELECT DISTINCT pm.member.memberId
        FROM PensionMemberScore pm
        JOIN PlaceMemberScore pl ON pm.member.memberId = pl.member.memberId
        WHERE pm.pension.pensionId = :pensionId
          AND pl.place.placeId = :placeId
    """)
    List<Long> findCommonMembers(Long pensionId, Long placeId);


    @Query("""
        SELECT
            pms.pension.pensionId, pms2.place.placeId, pms.member.memberId
        FROM
            PensionMemberScore pms
        JOIN
            PlaceMemberScore pms2 ON pms.member.memberId = pms2.member.memberId
        WHERE
            pms.pension.pensionId IN :pensionIds AND pms2.place.placeId IN :placeIds
        
    """)
    List<Object[]> findCommonMembersBatch(List<Long> pensionIds, List<Long> placeIds);

    @Query("SELECT DISTINCT p.pensionPlaceId.pensionId FROM PensionPlaceScore p")
    List<Long> findPensionIds();
}
