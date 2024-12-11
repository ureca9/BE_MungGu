package com.meong9.backend.domain.recommendation.recommendation.service;

import com.meong9.backend.domain.address.repository.PlcPenAddressRepository;
import com.meong9.backend.domain.member.repository.FavoriteRegionRepository;
import com.meong9.backend.domain.member.repository.PlcFavCategoryRepository;
import com.meong9.backend.domain.place.repository.PlcCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContentBasedRecommendation {

    private final FavoriteRegionRepository favoriteRegionRepository;
    private final PlcFavCategoryRepository plcFavCategoryRepository;
    private final PlcCategoryRepository plcCategoryRepository;
    private final PlcPenAddressRepository plcPenAddressRepository;

    public Map<Long, Double> calculateContentScores(Long userId, List<Long> allPensionIds, String type) {
        // 선호 지역 및 카테고리 가져오기
        log.info("사용자 ID: {}의 콘텐츠 기반 점수 계산 시작", userId);
        List<Long> favoriteRegions = favoriteRegionRepository.findRegionIdsByMemberId(userId);
        log.info("사용자 ID {}의 선호 지역: {}", userId, favoriteRegions);

        List<Long> favoriteCategories = plcFavCategoryRepository.findCategoryIdsByMemberId(userId);
        log.info("사용자 ID {}의 선호 카테고리: {}", userId, favoriteCategories);

        // 선호 지역에 속한 펜션 ID 가져오기
        List<Long> regionMatchedPensions = plcPenAddressRepository.findPensionIdsByRegionIds(favoriteRegions);
        log.info("선호 지역에 해당하는 펜션 ID: {}", regionMatchedPensions);

        Map<Long, Double> contentScores = new HashMap<>();
        for (Long pensionId : allPensionIds) {
            log.debug("펜션 ID {}의 점수 계산 시작", pensionId);
            double score = 0.0;

            // 지역 선호도를 기반으로 점수 부여
            if (regionMatchedPensions.contains(pensionId)) {
                log.debug("펜션 ID {}가 선호 지역에 해당합니다.", pensionId);
                score += 1.0;
            } else {
                log.debug("펜션 ID {}가 선호 지역에 해당하지 않습니다.", pensionId);
            }

            // 카테고리 선호도를 기반으로 점수 부여 (Place 유형에만 적용)
            if ("Place".equals(type)) {
                if (categoryMatches(pensionId, favoriteCategories)) {
                    log.debug("펜션 ID {}가 선호 카테고리에 해당합니다.", pensionId);
                    score += 1.0;
                } else {
                    log.debug("펜션 ID {}가 선호 카테고리에 해당하지 않습니다.", pensionId);
                }
            }

            // -1 ~ 1로 정규화
            score = Math.max(-1.0, Math.min(score, 1.0));
            log.debug("펜션 ID {}의 최종 정규화 점수: {}", pensionId, score);

            contentScores.put(pensionId, score);
        }

        log.info("사용자 ID {}의 최종 콘텐츠 기반 점수: {}", userId, contentScores);
        return contentScores;
    }

    private boolean categoryMatches(Long pensionId, List<Long> favoriteCategories) {
        // 해당 펜션의 카테고리가 사용자의 선호 카테고리와 일치하는지 확인
        List<Long> pensionCategories = plcCategoryRepository.findCategoryIdsByPensionId(pensionId);
        log.debug("펜션 ID {}의 카테고리: {}", pensionId, pensionCategories);

        boolean matches = favoriteCategories.stream().anyMatch(pensionCategories::contains);
        log.debug("펜션 ID {}가 선호 카테고리에 해당하는지: {}", pensionId, matches);

        return matches;
    }
}
