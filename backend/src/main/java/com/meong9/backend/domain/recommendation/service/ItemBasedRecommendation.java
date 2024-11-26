package com.meong9.backend.domain.recommendation.service;

import lombok.RequiredArgsConstructor;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ItemBasedRecommendation {

    public List<RecommendedItem> recommend(DataModel dataModel, long facilityId, int numRecommendations) throws Exception {
        // 시설 간 유사도 계산
        ItemSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);
        // Item-Based 추천 생성
        GenericItemBasedRecommender recommender = new GenericItemBasedRecommender(dataModel, similarity);

        return recommender.recommend(facilityId, numRecommendations);
    }
}

