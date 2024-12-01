package com.meong9.backend.domain.review.dto;

import com.meong9.backend.domain.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Getter
@Builder
public class ReviewDetailsResponseDto {
    private Long reviewId;
    private String content;
    private Float score;
    private Date visitDate;
    private String type;
    private Long plcPenId;
    private String nickname;
    private List<FileResponseDto> file;

    public static ReviewDetailsResponseDto from(Review review) {
        return ReviewDetailsResponseDto.builder()
                .reviewId(review.getReviewId())
                .content(review.getContent())
                .score(review.getScore())
                .visitDate(review.getVisitDate())
                .type(review.getType())
                .plcPenId(review.getPlacePensionId())
                .nickname(review.getNickname())
                .file(review.getReviewFiles().stream()
                        .map(FileResponseDto::from)
                        .toList())
                .build();
    }
}
