package com.meong9.backend.domain.recommendation.recommendation.repository;

import com.meong9.backend.domain.recommendation.recommendation.entity.PlaceRecommendation;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceRecommendationRepository extends JpaRepository<PlaceRecommendation, Long> {

    @Query("SELECT p FROM PlaceRecommendation p WHERE p.pension.pensionId = :pensionId ORDER BY p.score DESC")
    List<PlaceRecommendation> findByPensionId(@Param("pensionId") Long pensionId, Sort score);
}
