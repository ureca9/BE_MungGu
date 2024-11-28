package com.meong9.backend.domain.recommendation.recommendation.repository;

import com.meong9.backend.domain.recommendation.recommendation.entity.PensionRecommendation;
import org.springframework.data.domain.Sort;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PensionRecommendationRepository extends JpaRepository<PensionRecommendation, Long> {


    @Query("""
        SELECT p
        FROM PensionRecommendation p
        LEFT JOIN FETCH p.pension ps
        LEFT JOIN FETCH ps.pensionFiles pf
        LEFT JOIN FETCH pf.mediaFile mf
        WHERE p.member.memberId = :memberId
        ORDER BY p.score DESC
    """)
    List<PensionRecommendation> findByMemberId(@Param("memberId") Long memberId);

}
