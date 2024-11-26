package com.meong9.backend.domain.recommendation.service;

import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserBasedRecommendation {

    public List<RecommendedItem> recommend(DataModel dataModel, long userId, int numRecommendations) throws Exception {
        // 사용자 간 유사도 계산
        UserSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);

        // 가장 가까운 사용자 이웃 선택 (5명)
        UserNeighborhood neighborhood = new NearestNUserNeighborhood(numRecommendations, similarity, dataModel);

        // 추천 엔진 생성
        GenericUserBasedRecommender recommender = new GenericUserBasedRecommender(dataModel, neighborhood, similarity);

        // 사용자 추천
        return recommender.recommend(userId, numRecommendations);
    }
}
