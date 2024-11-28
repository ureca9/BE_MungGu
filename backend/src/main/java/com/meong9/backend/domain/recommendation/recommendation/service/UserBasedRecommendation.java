package com.meong9.backend.domain.recommendation.recommendation.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
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

        // 사용자 데이터 유효성 검사
        LongPrimitiveIterator userIDs = dataModel.getUserIDs();
        boolean userExists = false;

        while (userIDs.hasNext()) {
            if (userIDs.nextLong() == userId) {
                userExists = true;
                break;
            }
        }

        if (!userExists) {
            log.warn("DataModel에 사용자 {}가 없습니다. 추천을 건너뜁니다.", userId);
            return List.of();
        }


        // 사용자 간 유사도 계산 - 피어슨 상관계수로 유사도 계산(-1~1)
        UserSimilarity similarity = new PearsonCorrelationSimilarity(dataModel);
        log.info("유사도 정보 (사용자별 유사도):");
        for (LongPrimitiveIterator it = dataModel.getUserIDs(); it.hasNext(); ) {
            long otherUserId = it.next();
            if (userId != otherUserId) {
                double score = similarity.userSimilarity(userId, otherUserId);
                log.info("User {} -> User {} 유사도: {}", userId, otherUserId, score);
            }
        }

        // 가장 가까운 사용자 이웃 선택 (num 명)
        UserNeighborhood neighborhood = new NearestNUserNeighborhood(numRecommendations, similarity, dataModel);
        long[] neighbors = neighborhood.getUserNeighborhood(userId);
        log.info("사용자 {}의 이웃: {}", userId, neighbors);

        GenericUserBasedRecommender recommender = new GenericUserBasedRecommender(dataModel, neighborhood, similarity);
        log.info("추천: " + recommender);

        // 사용자 추천
        List<RecommendedItem> recommendations = recommender.recommend(userId, numRecommendations-8);
        log.info("추천 결과 디버그 정보:");
        recommendations.forEach(item -> log.info("추천 아이템 ID: {}, 점수: {}", item.getItemID(), item.getValue()));


        // 추천 결과 로그 출력
        if (recommendations.isEmpty()) {
            log.info("No recommendations available for userId: {}", userId);
        } else {
            log.info("success Recommendations for userId111 {}: ", userId);
            recommendations.forEach(item -> log.info("Item ID: {}, Score: {}", item.getItemID(), item.getValue()));
        }

        return recommendations;
    }
}
