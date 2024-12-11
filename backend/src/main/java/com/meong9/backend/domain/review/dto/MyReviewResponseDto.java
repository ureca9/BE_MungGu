package com.meong9.backend.domain.review.dto;

import com.meong9.backend.domain.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Getter
@Builder
public class MyReviewResponseDto {
    private Long reviewId;
    private String plcPenName;
    private String content;
    private Float score;
    private LocalDate visitDate;
    private String type;
    private Long plcPenId;
    private String nickname;
    private List<FileResponseDto> file;

    public static MyReviewResponseDto from(Review review,String plcPenName) {

        return MyReviewResponseDto.builder()
                .reviewId(review.getReviewId())
                .plcPenName(plcPenName)
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
