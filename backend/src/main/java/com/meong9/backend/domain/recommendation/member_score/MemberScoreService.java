package com.meong9.backend.domain.recommendation.member_score;

import com.meong9.backend.domain.recommendation.member_score.pension_member_score.repository.PensionMemberScoreRepository;
import com.meong9.backend.domain.recommendation.member_score.place_member_score.repository.PlaceMemberScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberScoreService {

    private final PensionMemberScoreRepository pensionMemberScoreRepository;
    private final PlaceMemberScoreRepository placeMemberScoreRepository;


    public void cleanupOldScores() {
        LocalDateTime threshold = LocalDateTime.now().minusMonths(36); // 3년 기준
        long deletedPensionScores = pensionMemberScoreRepository.deleteByLastUpdatedAtBefore(threshold);
        long deletedPlaceScores = placeMemberScoreRepository.deleteByLastUpdatedAtBefore(threshold);
        log.info("Deleted {} pension scores and {} place scores older than {}", deletedPensionScores, deletedPlaceScores, threshold);
    }

//    // 좋아요 등록 시 점수 추가
//    @Transactional
//    public void addLike(Long memberId, Long placeId) {
//        log.info("Adding like - Member ID: {}, Place ID: {}", memberId, placeId);
//        updateScore(memberId, placeId, 3); // 좋아요 점수는 3점
//    }
//
//    // 좋아요 삭제 시 점수 차감
//    @Transactional
//    public void removeLike(Long memberId, Long placeId) {
//        log.info("Removing like - Member ID: {}, Place ID: {}", memberId, placeId);
//        updateScore(memberId, placeId, -3); // 좋아요 점수 제거
//    }
//
//    // 리뷰 등록 시 점수 추가
//    @Transactional
//    public void addReview(Long memberId, Long placeId, float rating) {
//        log.info("Adding review - Member ID: {}, Place ID: {}, Rating: {}", memberId, placeId, rating);
//        int reviewScore = calculateWeightFromRating(rating);
//        updateScore(memberId, placeId, reviewScore);
//    }
//
//    // 리뷰 수정 시 점수 변경
//    @Transactional
//    public void updateReview(Long memberId, Long placeId, float oldRating, float newRating) {
//        log.info("Updating review - Member ID: {}, Place ID: {}, Old Rating: {}, New Rating: {}", memberId, placeId, oldRating, newRating);
//        int oldScore = calculateWeightFromRating(oldRating);
//        int newScore = calculateWeightFromRating(newRating);
//        updateScore(memberId, placeId, newScore - oldScore); // 점수 차이만큼 업데이트
//    }
//
//    // 리뷰 삭제 시 점수 차감
//    @Transactional
//    public void deleteReview(Long memberId, Long placeId, float rating) {
//        log.info("Deleting review - Member ID: {}, Place ID: {}, Rating: {}", memberId, placeId, rating);
//        int reviewScore = calculateWeightFromRating(rating);
//        updateScore(memberId, placeId, -reviewScore); // 기존 리뷰 점수 제거
//    }
//
//    // 리뷰 점수 가중치 계산
//    private int calculateWeightFromRating(float rating) {
//        if (rating >= 0.0f && rating < 1.5f) {
//            return -5; // 매우 나쁨
//        } else if (rating >= 1.5f && rating < 2.5f) {
//            return -2; // 나쁨
//        } else if (rating >= 2.5f && rating < 3.5f) {
//            return 0;  // 보통
//        } else if (rating >= 3.5f && rating < 4.5f) {
//            return 5;  // 좋음
//        } else if (rating >= 4.5f && rating <= 5.0f) {
//            return 10; // 매우 좋음
//        } else {
//            return 0; // 잘못된 평점은 무시
//        }
//    }
//
//    // 점수 업데이트 - 시설
//    private void updateScore(Long memberId, Long placeId, int scoreDelta) {
//        PlaceMemberId id = PlaceMemberId.builder()
//                .memberId(memberId)
//                .placeId(placeId)
//                .build();
//
//        // 기존 점수 가져오기 또는 초기화
//        PlaceMemberScore memberScore = placeMemberScoreRepository.findById(id)
//                .orElse(PlaceMemberScore.builder() // 기본
//                        .placeMemberId(id)
//                        .score(0)
//                        .lastUpdatedAt(LocalDateTime.now())
//                        .build());
//
//        // 점수 업데이트
//        memberScore.setScore(memberScore.getScore() + scoreDelta);
//
//        // 점수 저장
//        placeMemberScoreRepository.save(memberScore);
//    }


}
