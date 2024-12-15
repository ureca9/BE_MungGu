package com.meong9.backend.domain.review.dto;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.review.entity.Review;
import lombok.*;
import org.springframework.data.domain.Slice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

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
                .profileImageUrl(member != null && member.getProfileImage() != null
                        ? member.getProfileImage().getFileUrl()
                        : null)
                .content(review.getContent())
                .score(review.getScore() != null
                        ? new BigDecimal(Float.toString(review.getScore()))
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue() // Double 타입으로 변환
                        : null)
                .visitDate(review.getVisitDate() != null ? review.getVisitDate().toString() : null)
                .nickname(member != null ? member.getNickname() : null)
                .file(files)
                .build();
    }

    /**
     * 리뷰 리스트를 ReviewSummaryResponseDto 리스트로 변환하는 메서드.
     *
     * @param reviews 리뷰 리스트 (Slice 객체)
     * @return ReviewSummaryResponseDto 리스트
     */
    public static List<ReviewSummaryResponseDto> fromList(Slice<Review> reviews) {
        return reviews.stream()
                .map(review -> {
                    // 리뷰 파일 리스트 변환
                    List<ReviewSummaryFileDto> files = review.getReviewFiles().stream()
                            .map(file -> ReviewSummaryFileDto.builder()
                                    .mediaFileId(file.getFile().getMediaFileId())
                                    .fileType(file.getFile().getFileType().name())
                                    .fileUrl(file.getFile().getFileUrl())
                                    .build())
                            .collect(Collectors.toList());
                    // ReviewSummaryResponseDto 생성
                    return ReviewSummaryResponseDto.from(
                            review,
                            files,
                            review.getMember() // Member 정보 전달
                    );
                })
                .collect(Collectors.toList());
    }
}
