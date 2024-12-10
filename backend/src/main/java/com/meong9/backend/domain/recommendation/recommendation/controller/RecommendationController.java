package com.meong9.backend.domain.recommendation.recommendation.controller;

import com.meong9.backend.domain.like.service.LikeService;
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
    private final LikeService likeService;

    @GetMapping("/spots/recommendations")
    public ResponseEntity<?> recommendPensions(@CurrentMember Member member) {
        Map<String, List<RecommendationDto>> recommend = new HashMap<>();

        if(member == null) { // 로그인 되지 않은 사용자
            // 좋아요 인기 펜션
            List<RecommendationDto> recommendItem = likeService.getTopLikedPensions();
            recommend.put("recommend", recommendItem);
        } else { // 로그인 된 사용자
            List<RecommendationDto> recommendItem = recommendationService.getPensionRecommendations(member, 5);
            recommend.put("recommend", recommendItem);
        }
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
