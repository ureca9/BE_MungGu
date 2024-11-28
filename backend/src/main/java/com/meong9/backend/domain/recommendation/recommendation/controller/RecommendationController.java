package com.meong9.backend.domain.recommendation.recommendation.controller;

import com.meong9.backend.domain.recommendation.recommendation.entity.PensionRecommendation;
import com.meong9.backend.domain.recommendation.recommendation.entity.PlaceRecommendation;
import com.meong9.backend.domain.recommendation.recommendation.service.RecommendationService;
import com.meong9.backend.global.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/api/v1/spots/recommendations")
    public ResponseEntity<?> recommendPensions(@RequestParam long userId, @RequestParam int numRecommendations) throws Exception {
        List<PensionRecommendation> recommendItem = recommendationService.getPensionRecommendations(userId, numRecommendations);

        return CommonResponse.ok("success", recommendItem);
    }

    @GetMapping("/recommend/places")
    public List<PlaceRecommendation> recommendPlaces(@RequestParam long pensionId, @RequestParam int numRecommendations) throws Exception {
        return recommendationService.getPlaceRecommendations(pensionId, numRecommendations);
    }
}
