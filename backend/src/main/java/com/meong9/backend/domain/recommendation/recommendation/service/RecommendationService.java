package com.meong9.backend.domain.recommendation.recommendation.service;

import com.meong9.backend.domain.member.repository.MemberRepository;
import com.meong9.backend.domain.recommendation.member_score.place_member_score.repository.PlaceMemberScoreRepository;
import com.meong9.backend.domain.recommendation.recommendation.dto.PensionRecommendationDto;
import com.meong9.backend.domain.recommendation.recommendation.entity.PensionRecommendation;
import com.meong9.backend.domain.recommendation.recommendation.entity.PlaceRecommendation;
import com.meong9.backend.domain.recommendation.recommendation.repository.PensionRecommendationRepository;
import com.meong9.backend.domain.recommendation.recommendation.repository.PlaceRecommendationRepository;
import com.meong9.backend.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.impl.model.jdbc.ReloadFromJDBCDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class RecommendationService {

    @Qualifier("pensionDataModel")
    private final DataModel pensionDataModel;

    @Qualifier("placeDataModel")
    private final DataModel placeDataModel;

    private final UserBasedRecommendation userBasedRecommendation;
    private final ItemBasedRecommendation itemBasedRecommendation;
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final PensionRecommendationRepository pensionRecommendationRepository;
    private final PlaceRecommendationRepository placeRecommendationRepository;

    public RecommendationService(@Qualifier("pensionDataModel")DataModel pensionDataModel, @Qualifier("placeDataModel")DataModel placeDataModel,
                                 UserBasedRecommendation userBasedRecommendation, ItemBasedRecommendation itemBasedRecommendation,
                                 ReviewRepository reviewRepository, MemberRepository memberRepository,
                                 PensionRecommendationRepository pensionRecommendationRepository, PlaceRecommendationRepository placeRecommendationRepository) {
        this.pensionDataModel = pensionDataModel;
        this.placeDataModel = placeDataModel;
        this.userBasedRecommendation = userBasedRecommendation;
        this.itemBasedRecommendation = itemBasedRecommendation;
        this.reviewRepository = reviewRepository;
        this.memberRepository = memberRepository;
        this.pensionRecommendationRepository = pensionRecommendationRepository;
        this.placeRecommendationRepository = placeRecommendationRepository;
    }

    // 사용자 기반 펜션 추천 (User-Based)
    public List<RecommendedItem> processUserRecommendations(Long userId) {
        log.info("Processing recommendations for userId: {}", userId); // 로그 추가
        try {
            if (pensionDataModel instanceof ReloadFromJDBCDataModel) {
                ReloadFromJDBCDataModel model = (ReloadFromJDBCDataModel) pensionDataModel;
                model.refresh(null); // 데이터 새로고침

                log.info("DataModel 새로고침.");
            }
            return userBasedRecommendation.recommend(pensionDataModel, userId, 10);
        } catch (Exception e) {
            log.error("Error processing user recommendations for userId: {}", userId, e);
            return List.of();
        }
    }


    // 특정 펜션에 대한 시설 추천 생성 (Item-Based)
    public List<RecommendedItem> recommendFacilitiesForPension(Long pensionId) {
        try {
            return itemBasedRecommendation.recommend(placeDataModel, pensionId, 10);
        } catch (Exception e) {
            log.error("Error recommending facilities for pensionId: {}", pensionId, e);
            return List.of();
        }
    }

    // 리뷰가 1개 이상 달린 펜션 ID 조회
    public List<Long> getPensionsWithReviews() {
        log.info("로그 1개 이상 달린 펜션 ID");
        return reviewRepository.findPensionReviewCount(1);
    }

    // 활성 사용자 ID 조회
    public List<Long> getActiveMemberIds() {
        LocalDateTime week = LocalDateTime.now().minusWeeks(1); // 일주일 간 접속한 사람만
        log.info("일주일 간 접속한 사용자");
        List<Long> memberIds = memberRepository.findActiveMembers(week);
        log.info("Retrieved active member IDs: {}", memberIds);
        return memberIds;
    }

    // 추천 테이블 아이템 반환 (펜션)
    public List<PensionRecommendation> getPensionRecommendations(Long memberId, int maxItems) {
        List<PensionRecommendation> recommendations = getPensionRecommendationItem(memberId, maxItems);
        for(PensionRecommendation pensionRecommendation : recommendations){
            PensionRecommendationDto pensionRecommendationDto = PensionRecommendationDto.builder()
                    .id(pensionRecommendation.getPensionMemberId().getPensionId())
                    .name(pensionRecommendation.getPension().getName())
                    .address(pensionRecommendation.getPension().get)
                    .img(pensionRecommendation.getPension().get)
                    .reviewAvg(pensionRecommendation.getPension().getReviewAvg())
                    .reviewCount(pensionRecommendation.getPension().getReviewCount())
                    .build();
        }
    }

    // 추천 테이블 아이템 반환 (시설)
    public List<PlaceRecommendation> getPlaceRecommendations(Long pensionId, int maxItems) {
        List<PlaceRecommendation> recommendations = getPlaceRecommendationItem(pensionId, maxItems);
    }

    // 추천 테이블 아이템 반환 (펜션)
    private List<PensionRecommendation> getPensionRecommendationItem(Long memberId, int maxItems) {
        List<PensionRecommendation> recommendations = pensionRecommendationRepository
                .findByMemberId(memberId, Sort.by("score").descending());

        // recommendations의 크기가 maxItems보다 작거나 같으면 전체 반환
        if (recommendations.size() <= maxItems) {
            return recommendations;
        }

        // recommendations의 크기가 maxItems보다 크면 maxItems만큼 반환
        return recommendations.subList(0, maxItems);
    }

    // 추천 테이블 아이템 반환 (시설)
    private List<PlaceRecommendation> getPlaceRecommendationItem(Long pensionId, int maxItems) {
        List<PlaceRecommendation> recommendations = placeRecommendationRepository
                .findByPensionId(pensionId, Sort.by("score").descending());

        // recommendations의 크기가 maxItems보다 작거나 같으면 전체 반환
        if (recommendations.size() <= maxItems) {
            return recommendations;
        }

        // recommendations의 크기가 maxItems보다 크면 maxItems만큼 반환
        return recommendations.subList(0, maxItems);
    }

}
