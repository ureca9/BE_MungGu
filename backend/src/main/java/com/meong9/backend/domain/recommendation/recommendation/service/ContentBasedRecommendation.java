package com.meong9.backend.domain.recommendation.recommendation.service;

import com.meong9.backend.domain.address.repository.PlcPenAddressRepository;
import com.meong9.backend.domain.member.repository.FavoriteRegionRepository2;
import com.meong9.backend.domain.member.repository.PlcFavCategoryRepository2;
import com.meong9.backend.domain.place.repository.PlaceRepository;
import com.meong9.backend.domain.recommendation.recommendation.projection.PlcPenProjection;
import com.meong9.backend.global.utils.DistanceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ContentBasedRecommendation {

    private final FavoriteRegionRepository2 favoriteRegionRepository;
    private final PlcFavCategoryRepository2 plcFavCategoryRepository;
    private final PlcPenAddressRepository plcPenAddressRepository;
    private final PlaceRepository placeRepository;

    public Map<Long, Double> calculateContentScores(Long userId, List<Long> allPensionIds, String type) {
        // 선호 지역 및 카테고리 가져오기
//        log.info("사용자 ID: {}의 콘텐츠 기반 점수 계산 시작", userId);
        List<Long> favoriteRegions = favoriteRegionRepository.findRegionIdsByMemberId(userId);
//        log.info("사용자 ID {}의 선호 지역: {}", userId, favoriteRegions);

        List<Long> favoriteCategories = plcFavCategoryRepository.findCategoryIdsByMemberId(userId);
//        log.info("사용자 ID {}의 선호 카테고리: {}", userId, favoriteCategories);

        // 선호 지역에 속한 펜션 ID 가져오기
        List<Long> regionMatchedPensions = plcPenAddressRepository.findPensionIdsByRegionIds(favoriteRegions);
//        log.info("선호 지역에 해당하는 펜션 ID: {}", regionMatchedPensions);

        Map<Long, Double> contentScores = new HashMap<>();
        for (Long pensionId : allPensionIds) {
//            log.debug("펜션 ID {}의 점수 계산 시작", pensionId);
            double score = 0.0;

            // 지역 선호도를 기반으로 점수 부여
            if (regionMatchedPensions.contains(pensionId)) {
//                log.debug("펜션 ID {}가 선호 지역에 해당합니다.", pensionId);
                score += 1.0;
            } else {
//                log.debug("펜션 ID {}가 선호 지역에 해당하지 않습니다.", pensionId);
            }

            // -1 ~ 1로 정규화
            score = Math.max(-1.0, Math.min(score, 1.0));
            log.debug("펜션 ID {}의 최종 정규화 점수: {}", pensionId, score);

            contentScores.put(pensionId, score);
        }

//        log.info("사용자 ID {}의 최종 콘텐츠 기반 점수: {}", userId, contentScores);
        return contentScores;
    }

    public Map<Long, Double> calculateContentScoresByDistance(Map<String, List<PlcPenProjection>> placeInfos, PlcPenProjection pensionInfo) {
        // 결과를 저장할 Map
        Map<Long, Double> scoresByDistance = new HashMap<>();

        // 펜션의 위도, 경도, 그리고 지역 (province)
        Double targetLatitude = pensionInfo.getLatitude();
        Double targetLongitude = pensionInfo.getLongitude();
        String targetProvince = pensionInfo.getProvince();

        // 유효성 검사: targetProvince가 null인지 확인
        if (targetProvince == null) {
            log.warn("Error: pension의 province가 null입니다. ");
            return scoresByDistance; // 빈 Map 반환
        }

        // placeInfos(시설)에서 targetProvince(펜션)에 해당하는 리스트 가져오기
        List<PlcPenProjection> placeInfoList = placeInfos.getOrDefault(targetProvince, new ArrayList<>());

        // 위도 또는 경도가 null일 경우 랜덤 점수 부여
        if (targetLatitude == null || targetLongitude == null) {
            log.warn("펜션 위도, 경도 값이 null입니다. ID : {}", pensionInfo.getId());

            // 고정 점수
            double fixedScore = 0.000001;

            for (PlcPenProjection itemInfo : placeInfoList) {
                if (itemInfo == null) {
                    log.warn("Skip: 아이템(시설) 정보가 null입니다. ");
                    continue;
                }

//                log.info("content score: {} placeId: {}", fixedScore, itemInfo.getId());
                scoresByDistance.put(itemInfo.getId(), fixedScore);
            }

            return scoresByDistance; // 결과 반환
        }

        // 위도와 경도가 유효할 경우 거리 점수 계산
        for (PlcPenProjection itemInfo : placeInfoList) {
            if (itemInfo == null) {
//                log.warn("Skip: 아이템(시설) 정보가 null입니다.");
                continue;
            }

            // 시설의 위도와 경도
            Double itemLatitude = itemInfo.getLatitude();
            Double itemLongitude = itemInfo.getLongitude();

            // 유효성 검사: 위도와 경도가 null인지 확인
            if (itemLatitude == null || itemLongitude == null) {
//                log.warn("Skip: 아이템(시설) 정보의 위도 or 경도가 null입니다.: {}");
                continue;
            }

            // 두 지점 간의 거리 계산 (Haversine Formula)
            double distance = DistanceMapper.calculateDistance(targetLatitude, targetLongitude, itemLatitude, itemLongitude) / 1000.0;

            // 거리 기반 점수 계산 (0~1)
            double score = 1 / (1 + distance);

//            log.info("content score: {} placeId: {}", score, itemInfo.getId());

            // 결과 저장
            scoresByDistance.put(itemInfo.getId(), score);
        }

        return scoresByDistance;
    }




}
