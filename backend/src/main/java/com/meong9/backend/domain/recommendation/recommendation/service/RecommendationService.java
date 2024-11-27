package com.meong9.backend.domain.recommendation.recommendation.service;

import com.meong9.backend.domain.member.repository.MemberRepository;
import com.meong9.backend.domain.plc_pen_review.repository.PlcPenReviewRepository;
import com.meong9.backend.domain.recommendation.recommendation.entity.PensionRecommendation;
import com.meong9.backend.domain.recommendation.recommendation.repository.PensionRecommendationRepository;
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
@RequiredArgsConstructor
@Slf4j
public class RecommendationService {

    @Qualifier("pensionDataModel")
    private final DataModel pensionDataModel;

    @Qualifier("placeDataModel")
    private final DataModel placeDataModel;

    private final UserBasedRecommendation userBasedRecommendation;
    private final ItemBasedRecommendation itemBasedRecommendation;
    private final PlcPenReviewRepository plcPenReviewRepository;
    private final MemberRepository memberRepository;
    private final PensionRecommendationRepository pensionRecommendationRepository;

    // 사용자 기반 펜션 추천 (User-Based)
    public List<RecommendedItem> processUserRecommendations(Long userId) {
        log.info("Processing recommendations for userId: {}", userId); // 로그 추가
        try {
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
        return plcPenReviewRepository.findPensionReviewCount(1);
    }

    // 활성 사용자 ID 조회
    public List<Long> getActiveMemberIds() {
        LocalDateTime week = LocalDateTime.now().minusWeeks(1); // 일주일 간 접속한 사람만
        log.info("일주일 간 접속한 사용자");
        List<Long> memberIds = memberRepository.findActiveMembers(week);
        log.info("Retrieved active member IDs: {}", memberIds);
        return memberIds;
    }

    public Page<PensionRecommendation> getRecommendations(Long memberId, int page) {
        Pageable pageable = PageRequest.of(page, 5, Sort.by("score").descending());
        return pensionRecommendationRepository.findByMemberId(memberId, pageable);
    }
}
