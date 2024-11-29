package com.meong9.backend.domain.recommendation.recommendation.controller;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.recommendation.recommendation.dto.RecommendationDto;
import com.meong9.backend.domain.recommendation.recommendation.entity.PensionRecommendation;
import com.meong9.backend.domain.recommendation.recommendation.entity.PlaceRecommendation;
import com.meong9.backend.domain.recommendation.recommendation.service.RecommendationService;
import com.meong9.backend.global.annotation.member.CurrentMember;
import com.meong9.backend.global.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/spots/recommendations")
    public ResponseEntity<?> recommendPensions(@CurrentMember Member member) {
        List<RecommendationDto> recommendItem = recommendationService.getPensionRecommendations(member, 5);

        Map<String, List<RecommendationDto>> recommend = new HashMap<>();
        recommend.put("recommend", recommendItem);

        return CommonResponse.ok("success", recommend);
    }

    @GetMapping("/pensions/{pensionId}/recommendations")
    public ResponseEntity<?> recommendPlaces(@PathVariable long pensionId) {
        List<RecommendationDto> recommendItem = recommendationService.getPlacecommendations(pensionId, 5);

        Map<String, List<RecommendationDto>> recommend = new HashMap<>();
        recommend.put("recommend", recommendItem);

        return CommonResponse.ok("success", recommend);
    }
}
