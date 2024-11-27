package com.meong9.backend.domain.recommendation.member_score.pension_place_score.service;

import com.meong9.backend.domain.pension.entity.Pension;
import com.meong9.backend.domain.pension.repository.PensionRepository;
import com.meong9.backend.domain.place.entity.Place;
import com.meong9.backend.domain.place.repository.PlaceRepository;
import com.meong9.backend.domain.plc_pen_review.repository.PlcPenReviewRepository;
import com.meong9.backend.domain.recommendation.id_class.PensionPlaceId;
import com.meong9.backend.domain.recommendation.member_score.pension_member_score.repository.PensionMemberScoreRepository;
import com.meong9.backend.domain.recommendation.member_score.pension_place_score.entity.PensionPlaceScore;
import com.meong9.backend.domain.recommendation.member_score.pension_place_score.repository.PensionPlaceScoreRepository;
import com.meong9.backend.domain.recommendation.member_score.place_member_score.entity.PlaceMemberScore;
import com.meong9.backend.domain.recommendation.member_score.place_member_score.repository.PlaceMemberScoreRepository;
import com.meong9.backend.domain.recommendation.recommendation.entity.PlaceRecommendation;
import com.meong9.backend.domain.recommendation.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PensionPlaceScoreService {

    private final PensionPlaceScoreRepository pensionPlaceScoreRepository;
    private final PlaceMemberScoreRepository placeMemberScoreRepository;
    private final PensionRepository pensionRepository;
    private final PlaceRepository placeRepository;
    private final RecommendationService recommendationService;
    private final PlcPenReviewRepository plcPenReviewRepository;
    private final PensionMemberScoreRepository pensionMemberScoreRepository;

    // 펜션-시설 점수를 초기화하고 업데이트하는 메서드
    // 리뷰가 있는 펜션을 대상으로 점수 초기화, 업데이트
    @Transactional
    public void initializeAndUpdateScores() {
        List<Long> pensionIds = plcPenReviewRepository.findPensionReviewCount(1);
        for (Long pensionId : pensionIds) {
            initializeOrUpdateScoresForPension(pensionId);
        }
    }

    // 특정 펜션의 점수를 초기화하거나 업데이트한다.
    private void initializeOrUpdateScoresForPension(Long pensionId) {
        List<Long> placeIds = placeMemberScoreRepository.findAllPlaceIds();
        for (Long placeId : placeIds) {
            List<Long> commonMembers = findCommonMembers(pensionId, placeId);
            if (!commonMembers.isEmpty()) {
                float score = calculateScore(commonMembers, pensionId, placeId);
                saveOrUpdatePensionPlaceScore(pensionId, placeId, score);
            }
        }
    }

    // 특정 펜션과 시설 간의 공통 멤버 조회
    private List<Long> findCommonMembers(Long pensionId, Long placeId) {
        return pensionPlaceScoreRepository.findCommonMembers(pensionId, placeId);
    }

    // 공통 멤버 기반 펜션 시설 간 점수 계산
    private float calculateScore(List<Long> commonMembers, Long pensionId, Long placeId) {
        float totalScore = 0.0f;
        for (Long memberId : commonMembers) {
            float pensionScore = pensionMemberScoreRepository.findScoreByPensionIdAndMemberId(pensionId, memberId);
            float placeScore = placeMemberScoreRepository.findScoreByPlaceIdAndMemberId(placeId, memberId);
            totalScore += (pensionScore + placeScore) / 2; // 평균 점수
        }
        return totalScore / commonMembers.size();
    }

    // 특정 펜션의 점수 업데이트
    public void updateScoresForPension(Long pensionId) {
        List<Long> placeIds = pensionPlaceScoreRepository.findPlaceIdsByPensionId(pensionId);
        for (Long placeId : placeIds) {
            float score = calculateMemberScoreForPlace(placeId);
            saveOrUpdatePensionPlaceScore(pensionId, placeId, score);
        }
    }

    // 특정 펜션에 대한 시설 추천 생성
    public List<PlaceRecommendation> recommendPlacesForPension(Long pensionId) {
        List<RecommendedItem> recommendations = recommendationService.recommendFacilitiesForPension(pensionId);
        return recommendations.stream()
                .map(item -> PlaceRecommendation.builder()
                        .pensionPlaceId(new PensionPlaceId(pensionId, item.getItemID()))
                        .score(item.getValue())
                        .build())
                .toList();
    }

    // 특정 시설에 대한 사용자 점수 계산
    private float calculateMemberScoreForPlace(Long placeId) {
        List<PlaceMemberScore> scores = placeMemberScoreRepository.findByPlaceId(placeId);
        return (float) scores.stream()
                .mapToDouble(PlaceMemberScore::getScore)
                .average()
                .orElse(0.0);
    }

    // 펜션-시설 점수 저장, 업데이트
    private void saveOrUpdatePensionPlaceScore(Long pensionId, Long placeId, float score) {
        Pension pension = pensionRepository.getReferenceById(pensionId);
        Place place = placeRepository.getReferenceById(placeId);

        PensionPlaceId pensionPlaceId = new PensionPlaceId(pensionId, placeId);
        PensionPlaceScore pensionPlaceScore = pensionPlaceScoreRepository.findById(pensionPlaceId)
                .orElse(PensionPlaceScore.builder()
                        .pensionPlaceId(pensionPlaceId)
                        .pension(pension)
                        .place(place)
                        .score(0.0f)
                        .lastUpdatedAt(LocalDateTime.now())
                        .build());

        pensionPlaceScore.setScore(score);
        pensionPlaceScore.setLastUpdatedAt(LocalDateTime.now());
        pensionPlaceScoreRepository.save(pensionPlaceScore);
    }
}

