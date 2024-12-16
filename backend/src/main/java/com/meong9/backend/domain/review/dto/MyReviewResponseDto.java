package com.meong9.backend.domain.review.dto;

import com.meong9.backend.domain.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Getter
public class MyReviewResponseDto {
    private final Long reviewId;
    private final String content;
    private final Float score;
    private final LocalDate visitDate;
    private final String type;
    private final Long plcPenId;
    private final String nickname;
    private final FileResponseDto file;
    @Setter
    private String plcPenName;

    public MyReviewResponseDto(Long reviewId, String content, Float score, LocalDate visitDate, String type, Long plcPenId, String nickname, FileResponseDto file) {
        this.reviewId = reviewId;
        this.content = content;
        this.score = score;
        this.visitDate = visitDate;
        this.type = type;
        this.plcPenId = plcPenId;
        this.nickname = nickname;
        this.file = file;
    }
}
