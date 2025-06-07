package com.meong9.backend.domain.recommendation.member_score;

import com.meong9.backend.domain.member.repository.MemberRepository;
import com.meong9.backend.domain.pension.repository.PensionRepository;
import com.meong9.backend.domain.place.repository.PlaceRepository;
import com.meong9.backend.domain.recommendation.id_class.PensionMemberId;
import com.meong9.backend.domain.recommendation.id_class.PlaceMemberId;
import com.meong9.backend.domain.recommendation.member_score.pension_member_score.entity.PensionMemberScore;
import com.meong9.backend.domain.recommendation.member_score.pension_member_score.repository.PensionMemberScoreRepository;
import com.meong9.backend.domain.recommendation.member_score.place_member_score.entity.PlaceMemberScore;
import com.meong9.backend.domain.recommendation.member_score.place_member_score.repository.PlaceMemberScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberScoreService {

    private final PensionMemberScoreRepository pensionMemberScoreRepository;
    private final PlaceMemberScoreRepository placeMemberScoreRepository;
    private final MemberRepository memberRepository;
    private final PlaceRepository placeRepository;
    private final PensionRepository pensionRepository;

    // score 테이블 오래된 데이터 삭제
    public void cleanupOldScores() {
        LocalDateTime threshold = LocalDateTime.now().minusMonths(36); // 3년 기준
        long deletedPensionScores = pensionMemberScoreRepository.deleteByLastUpdatedAtBefore(threshold);
        long deletedPlaceScores = placeMemberScoreRepository.deleteByLastUpdatedAtBefore(threshold);
        log.info("Deleted {} pension scores and {} place scores older than {}", deletedPensionScores, deletedPlaceScores, threshold);
    }

    // 좋아요 등록 시 점수 추가
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

    // 리뷰 등록 비동기 처리
    @Async
    public void addReview(Long memberId, Long targetId, float rating, String type) {
        log.info("비동기로 리뷰를 통한 member 점수 등록 처리 시작: MemberId={}, TargetId={}", memberId, targetId);
        try {
            addReviewTransactional(memberId, targetId, rating, type);
        } catch (Exception e) {
            log.error("리뷰 등록 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addReviewTransactional(Long memberId, Long targetId, float rating, String type) {
        int reviewScore = calculateWeightFromRating(rating);
        if ("010".equals(type)) {
            updatePlaceScore(memberId, targetId, reviewScore);
        } else if ("020".equals(type)) {
            updatePensionScore(memberId, targetId, reviewScore);
        }
    }

    // 리뷰 수정 비동기 처리
    @Async
    public void updateReview(Long memberId, Long targetId, float oldRating, float newRating, String type) {
        log.info("비동기로 리뷰를 통한 member 점수 수정 처리 시작: MemberId={}, TargetId={}", memberId, targetId);
        try {
            updateReviewTransactional(memberId, targetId, oldRating, newRating, type);
        } catch (Exception e) {
            log.error("리뷰 수정 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateReviewTransactional(Long memberId, Long targetId, float oldRating, float newRating, String type) {
        int oldScore = calculateWeightFromRating(oldRating);
        int newScore = calculateWeightFromRating(newRating);

        if ("010".equals(type)) {
            updatePlaceScore(memberId, targetId, newScore);
        } else if ("020".equals(type)) {
            updatePensionScore(memberId, targetId, newScore);
        }
    }

    // 리뷰 삭제 비동기 처리
    @Async
    public void deleteReview(Long memberId, Long targetId, float rating, String type) {
        log.info("비동기로 리뷰를 통한 member 점수 삭제 처리 시작: MemberId={}, TargetId={}", memberId, targetId);
        try {
            deleteReviewTransactional(memberId, targetId, rating, type);
        } catch (Exception e) {
            log.error("리뷰 삭제 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteReviewTransactional(Long memberId, Long targetId, float rating, String type) {
        int reviewScore = calculateWeightFromRating(rating);

        if ("010".equals(type)) {
            deletePlaceScore(memberId, targetId);
        } else if ("020".equals(type)) {
            deletePensionScore(memberId, targetId);
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

    // 점수 업데이트 - 시설
    private void updatePlaceScore(Long memberId, Long placeId, int scoreDelta) {
        PlaceMemberId id = PlaceMemberId.builder()
                .memberId(memberId)
                .placeId(placeId)
                .build();

        // 기존 점수 가져오기 또는 초기화
        PlaceMemberScore memberScore = placeMemberScoreRepository.findById(id)
                .orElse(PlaceMemberScore.builder() // 기본
                        .placeMemberId(id)
                        .place(placeRepository.findById(placeId).orElseThrow(() -> new IllegalArgumentException("place가 존재하지 않습니다.")))
                        .member(memberRepository.findById(memberId).orElseThrow(()->new IllegalArgumentException("member가 존재하지 않습니다.")))
                        .score(0)
                        .lastUpdatedAt(LocalDateTime.now())
                        .build());

        // 점수 업데이트
        memberScore.setScore(scoreDelta);

        // 점수 저장
        placeMemberScoreRepository.save(memberScore);
    }

    // 점수 업데이트 - 펜션
    private void updatePensionScore(Long memberId, Long targetId, int scoreDelta) {
        PensionMemberId id = PensionMemberId.builder()
                .memberId(memberId)
                .pensionId(targetId)
                .build();

        // 기존 점수 가져오기 또는 초기화
        PensionMemberScore memberScore = pensionMemberScoreRepository.findById(id)
                .orElse(PensionMemberScore.builder() // 기본
                        .pensionMemberId(id)
                        .pension(pensionRepository.findById(targetId).orElseThrow(() -> new IllegalArgumentException("pension이 존재하지 않습니다.")))
                        .member(memberRepository.findById(memberId).orElseThrow(()->new IllegalArgumentException("member가 존재하지 않습니다.")))
                        .score(0)
                        .lastUpdatedAt(LocalDateTime.now())
                        .build());

        // 점수 업데이트
        memberScore.setScore(scoreDelta);

        // 점수 저장
        pensionMemberScoreRepository.save(memberScore);
    }

    // 점수 삭제 - 시설
    private void deletePlaceScore(Long memberId, Long placeId) {
        PlaceMemberId id = PlaceMemberId.builder()
                .memberId(memberId)
                .placeId(placeId)
                .build();

        // 데이터 존재 여부 확인 후 삭제
        if (placeMemberScoreRepository.existsById(id)) {
            placeMemberScoreRepository.deleteById(id);
            log.info("PlaceMemberScore 삭제 완료 - MemberId={}, PlaceId={}", memberId, placeId);
        } else {
            log.warn("PlaceMemberScore를 찾을 수 없음 - MemberId={}, PlaceId={}", memberId, placeId);
        }
    }

    // 점수 삭제 - 펜션
    private void deletePensionScore(Long memberId, Long pensionId) {
        PensionMemberId id = PensionMemberId.builder()
                .memberId(memberId)
                .pensionId(pensionId)
                .build();

        // 데이터 존재 여부 확인 후 삭제
        if (pensionMemberScoreRepository.existsById(id)) {
            pensionMemberScoreRepository.deleteById(id);
            log.info("PensionMemberScore 삭제 완료 - MemberId={}, PensionId={}", memberId, pensionId);
        } else {
            log.warn("PensionMemberScore를 찾을 수 없음 - MemberId={}, PensionId={}", memberId, pensionId);
        }
    }

}
