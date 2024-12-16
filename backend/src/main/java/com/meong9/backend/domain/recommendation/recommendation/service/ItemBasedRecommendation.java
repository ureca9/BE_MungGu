package com.meong9.backend.domain.recommendation.recommendation.service;

import com.meong9.backend.domain.recommendation.id_class.PensionPlaceId;
import com.meong9.backend.domain.recommendation.recommendation.entity.PlaceRecommendation;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemBasedRecommendation {
    @Qualifier("placeDataModel")
    private final DataModel placeDataModel;

    public ItemBasedRecommendation(@Qualifier("placeDataModel") DataModel placeDataModel) {
        this.placeDataModel = placeDataModel;
    }


    public List<RecommendedItem> recommend(DataModel dataModel, long pensionId, int numRecommendations) throws Exception {
//        log.info("아이템 기반 추천 시작 {} ", pensionId);
        // 사용자 데이터 유효성 검사
        if (!doesPensionExist(dataModel, pensionId)) {
//            log.warn("DataModel에 펜션ID {}가 없습니다. 추천을 건너뜁니다.", pensionId);
            return List.of();
        }

        if (dataModel.getPreferencesFromUser(pensionId).length() == 0) {
//            log.warn("pensionId {}와 관련된 평가 데이터가 없습니다. 추천을 건너뜁니다.", pensionId);
            return List.of();
        }

        // 시설 간 유사도 계산
        ItemSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);
        // 로그를 위한 코드
//        log.info("시설 간 유사도 계산 결과:");
//        LongPrimitiveIterator itemIDs = dataModel.getItemIDs();
//        while (itemIDs.hasNext()) {
//            long itemId1 = itemIDs.nextLong();
//            LongPrimitiveIterator otherItemIDs = dataModel.getItemIDs();
//
//            while (otherItemIDs.hasNext()) {
//                long itemId2 = otherItemIDs.nextLong();
//                if (itemId1 != itemId2) {
//                    double score = similarity.itemSimilarity(itemId1, itemId2);
//                    log.info("Item {} <-> Item {}: 유사도: {}", itemId1, itemId2, score);
//                }
//            }
//        }
        // Item-Based 객체 초기화
        GenericItemBasedRecommender recommender = new GenericItemBasedRecommender(dataModel, similarity);
        // 추천 생성
        List<RecommendedItem> recommendations = recommender.recommend(pensionId, numRecommendations);

//        log.info("item base 추천 pensionId: {}", pensionId);
//        recommendations.forEach(item -> log.info("추천된 place ID: {}, score: {}", item.getItemID(), item.getValue()));

//        if (recommendations.isEmpty()) {
//            log.warn("추천 결과가 없습니다. (pensionId: {})", pensionId);
//        }

        return recommendations;
    }

    // 특정 펜션에 대한 시설 추천 생성 (Item-Based)
    @Transactional(readOnly = true)
    public Map<Long, Double> recommendFacilitiesForPension(Long pensionId) {
        try {
            List<RecommendedItem> recommendedItems = recommend(placeDataModel, pensionId, 10);
//            return recommendations.stream()
//                    .map(item -> PlaceRecommendation.builder()
//                            .pensionPlaceId(new PensionPlaceId(pensionId, item.getItemID()))
//                            .score(item.getValue())
//                            .build())
//                    .toList();
            Map<Long, Double> scores = recommendedItems.stream()
                    .collect(Collectors.toMap(
                            RecommendedItem::getItemID,
                            item -> (double) item.getValue() // float -> Double 변환
                    ));

            // 점수 정규화
            return normalizeScores(scores);
        } catch (Exception e) {
            log.error("Error recommending facilities for pensionId: {}", pensionId, e);
            return new HashMap<>();
        }
    }

    private Map<Long, Double> normalizeScores(Map<Long, Double> scores) {
        if (scores.isEmpty()) {
            return scores; // 점수가 비어있으면 그대로 반환
        }

        // 최대값과 최소값 계산
        double max = 10.0; // 협업 필터링 점수의 최대값
        double min = -5.0; // 협업 필터링 점수의 최소값

        // 점수를 0~1 범위로 정규화
        return scores.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (entry.getValue() - min) / (max - min)
                ));
    }

    private boolean doesPensionExist(DataModel dataModel, long pensionId) {
        try {
            LongPrimitiveIterator userIDs = dataModel.getUserIDs();
            while (userIDs.hasNext()) {
                if (userIDs.nextLong() == pensionId) {
                    return true; // 존재하면 true 반환
                }
            }
        } catch (Exception e) {
            log.error("DataModel에서 pensionId {} 확인 중 오류 발생", pensionId, e);
        }
        return false; // 존재하지 않으면 false 반환
    }

}

