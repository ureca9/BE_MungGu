package com.meong9.backend.domain.member_score.place_member_score.service;

import com.meong9.backend.domain.like.place_like.entity.PlaceLike;
import com.meong9.backend.domain.like.place_like.repository.PlaceLikeRepository;
import com.meong9.backend.domain.member.repository.MemberRepository;
import com.meong9.backend.domain.member_score.id_class.PlaceMemberId;
import com.meong9.backend.domain.member_score.place_member_score.entity.PlaceMemberScore;
import com.meong9.backend.domain.member_score.place_member_score.repository.PlaceMemberScoreRepository;
import com.meong9.backend.domain.place.repository.PlaceRepository;
import com.meong9.backend.domain.plc_pen_review.entity.PlcPenReview;
import com.meong9.backend.domain.plc_pen_review.repository.PlcPenReviewRepository;
import com.meong9.backend.domain.review.entity.Review;
import com.meong9.backend.domain.review.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceMemberScoreService {

    private final PlaceMemberScoreRepository placeMemberScoreRepository;
    private final PlcPenReviewRepository plcPenReviewRepository;
    private final ReviewRepository reviewRepository;
    private final PlaceLikeRepository placeLikeRepository;
    private final MemberRepository memberRepository;
    private final PlaceRepository placeRepository;

    // 현재 리뷰와 좋아요 데이터를 기반으로 score 테이블 초기화
    @Transactional
    public void initializeScores() {
        // 1. 리뷰 데이터를 기반으로 점수 초기화
        initializeReviewScores();

        // 2. 좋아요 데이터를 기반으로 점수 초기화
        initializeLikeScores();
    }

    // 리뷰 데이터를 기반으로 점수 초기화
    private void initializeReviewScores() {
        // plc_pen_review에서 type이 "010"인 경우에 해당하는 리뷰 데이터 가져오기
        List<PlcPenReview> placeReviews = plcPenReviewRepository.findByType("010");

        // `for`문을 사용하여 리뷰 데이터 처리
        for (PlcPenReview reviewLink : placeReviews) {
            Long placeId = reviewLink.getPlcPenId();

            Review review = reviewRepository.findById(reviewLink.getReview().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid review ID: " + reviewLink.getReview().getId()));

            if (review.getMember() == null) {
                log.warn("Review has no associated member. Skipping review ID: {}", review.getId());
                continue; // 잘못된 리뷰는 건너뜀
            }

            Long memberId = review.getMember().getId();

            log.info("Processing review ID: {}", review.getId());
            log.info("Member ID: {}, Place ID: {}", memberId, placeId);

            float rating = review.getScore();
            int reviewScore = calculateWeightFromRating(rating);

            updateScore(memberId, placeId, reviewScore);
        }
    }

    // 좋아요 데이터를 기반으로 점수 초기화
    private void initializeLikeScores() {
        List<PlaceLike> allLikes = placeLikeRepository.findAll();

        for (PlaceLike like : allLikes) {
            Long memberId = like.getPlaceMemberId().getMemberId();
            Long placeId = like.getPlaceMemberId().getPlaceId();

            log.info("Processing like - Member ID: {}, Place ID: {}", memberId, placeId);

            updateScore(memberId, placeId, 3); // 좋아요는 3점 추가
        }
    }

    // 리뷰 점수 가중치 계산
    private int calculateWeightFromRating(float rating) {
        if (rating >= 0.0f && rating < 1.5f) {
            return -5; // 매우 나쁨
        } else if (rating >= 1.5f && rating < 2.5f) {
            return -2; // 나쁨
        } else if (rating >= 2.5f && rating < 3.5f) {
            return 0;  // 보통
        } else if (rating >= 3.5f && rating < 4.5f) {
            return 5;  // 좋음
        } else if (rating >= 4.5f && rating <= 5.0f) {
            return 10; // 매우 좋음
        } else {
            return 0; // 잘못된 평점은 무시
        }
    }

    // 점수 업데이트 메서드
    private void updateScore(Long memberId, Long placeId, int scoreDelta) {
        PlaceMemberId id = PlaceMemberId.builder()
                .memberId(memberId)
                .placeId(placeId)
                .build();


        // 기존 점수 가져오기 또는 초기화
        PlaceMemberScore memberScore = placeMemberScoreRepository.findById(id)
                .orElse(PlaceMemberScore.builder()
                        .placeMemberId(id)
                        .member(memberRepository.findById(memberId).orElse(null))
                        .place(placeRepository.findById(placeId).orElse(null))
                        .score(0) // 기본 점수는 0
                        .build());


        // 점수 업데이트
        memberScore.setScore(memberScore.getScore() + scoreDelta);

        log.info("Score: {}", scoreDelta);

        // 업데이트 시간 갱신
        memberScore.setLastUpdatedAt(LocalDateTime.now());

        // 점수 저장
        placeMemberScoreRepository.save(memberScore);
    }
}
