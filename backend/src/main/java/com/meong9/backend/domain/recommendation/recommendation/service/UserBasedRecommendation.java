package com.meong9.backend.domain.recommendation.recommendation.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class UserBasedRecommendation {

    public List<RecommendedItem> recommend(DataModel dataModel, long userId, int numRecommendations) throws Exception {
        // 사용자 간 유사도 계산 - 피어슨 상관계수로 유사도 계산(-1~1)
        UserSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);

        // 가장 가까운 사용자 이웃 선택 (num 명)
        UserNeighborhood neighborhood = new NearestNUserNeighborhood(numRecommendations, similarity, dataModel);

        GenericUserBasedRecommender recommender = new GenericUserBasedRecommender(dataModel, neighborhood, similarity);

        // 사용자 추천
        List<RecommendedItem> recommendations = recommender.recommend(userId, numRecommendations);

        // 추천 결과 로그 출력
        if (recommendations.isEmpty()) {
            log.info("No recommendations available for userId: {}", userId);
        } else {
            log.info("Recommendations for userId {}: ", userId);
            recommendations.forEach(item -> log.info("Item ID: {}, Score: {}", item.getItemID(), item.getValue()));
        }

        return recommendations;
    }
}
