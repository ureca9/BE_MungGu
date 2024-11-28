package com.meong9.backend.domain.recommendation.recommendation.repository;

import com.meong9.backend.domain.recommendation.recommendation.entity.PensionRecommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PensionRecommendationRepository extends JpaRepository<PensionRecommendation, Long> {

    @Query("SELECT p FROM PensionRecommendation p WHERE p.member.memberId = :memberId ORDER BY p.score DESC")
    List<PensionRecommendation> findByMemberId(Long memberId, Sort score);
}
