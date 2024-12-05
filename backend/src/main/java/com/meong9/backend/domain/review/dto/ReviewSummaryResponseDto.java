package com.meong9.backend.domain.review.dto;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.review.entity.Review;
import lombok.*;

import java.util.List;

/**
 * 리뷰 요약 정보를 전달하기 위한 DTO 클래스.
 *
 * 주요 필드:
 * - reviewId: 리뷰 ID
 * - profileImageUrl: 작성자의 프로필 이미지 URL
 * - content: 리뷰 내용
 * - score: 리뷰 평점
 * - visitDate: 방문 날짜
 * - nickname: 작성자 닉네임
 * - file: 리뷰에 첨부된 파일 정보
 */
@Getter
@Builder
public class ReviewSummaryResponseDto {
    private final Long reviewId; // 리뷰 ID
    private final String profileImageUrl; // 작성자의 프로필 이미지 URL
    private final String content; // 리뷰 내용
    private final Double score; // 리뷰 평점
    private final String visitDate; // 방문 날짜 (문자열로 포맷)
    private final String nickname; // 작성자 닉네임

    @Setter
    private List<ReviewSummaryFileDto> file; // 리뷰 첨부 파일 목록

    /**
     * Review 엔티티를 ReviewSummaryResponseDto로 변환하는 메서드.
     *
     * @param review Review 엔티티 객체
     * @return 변환된 ReviewSummaryResponseDto 객체
     */
    public static ReviewSummaryResponseDto from(Review review, List<ReviewSummaryFileDto> files, Member member) {
        return ReviewSummaryResponseDto.builder()
                .reviewId(review.getReviewId())
                .profileImageUrl(member.getProfileImage() != null ? member.getProfileImage().getFileUrl() : null)
                .content(review.getContent())
                .score(review.getScore() != null ? review.getScore().doubleValue() : null)
                .visitDate(review.getVisitDate() != null ? review.getVisitDate().toString() : null)
                .nickname(member.getNickname())
                .file(files)
                .build();
    }
}
