package com.meong9.backend.domain.recommendation.recommendation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ItemBasedRecommendation {

    public List<RecommendedItem> recommend(DataModel dataModel, long pensionId, int numRecommendations) throws Exception {
        // 시설 간 유사도 계산
        ItemSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);

        // Item-Based 객체 초기화
        GenericItemBasedRecommender recommender = new GenericItemBasedRecommender(dataModel, similarity);
        // 추천 생성
        List<RecommendedItem> recommendations = recommender.recommend(pensionId, numRecommendations);

        log.info("item base 추천 pensionId: {}", pensionId);
        recommendations.forEach(item -> log.info("추천된 place ID: {}, score: {}", item.getItemID(), item.getValue()));
        System.out.println("isEmpty(): "+recommendations.isEmpty());

        return recommendations;


    }
}

