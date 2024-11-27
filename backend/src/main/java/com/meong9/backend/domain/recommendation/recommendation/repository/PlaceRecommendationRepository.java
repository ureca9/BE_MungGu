package com.meong9.backend.domain.recommendation.recommendation.repository;

import com.meong9.backend.domain.recommendation.recommendation.entity.PlaceRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRecommendationRepository extends JpaRepository<PlaceRecommendation, Long> {
}
