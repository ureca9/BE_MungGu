package com.meong9.backend.domain.review.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class MyReviewResponseDto {
    private Long reviewId;
    private String content;
    private Float score;
    private LocalDate visitDate;
    private FileResponseDto file;
    private String type;
    private Long plcPenId;
    private String plcPenName;
    private String nickname;

    public MyReviewResponseDto(Long reviewId, String content, Float score, LocalDate visitDate,
                               String type, Long plcPenId, String plcPenName, String nickname, FileResponseDto file) {
        this.reviewId = reviewId;
        this.content = content;
        this.score = score;
        this.visitDate = visitDate;
        this.type = type;
        this.plcPenId = plcPenId;
        this.plcPenName = plcPenName;
        this.nickname = nickname;
        this.file = file;
    }
}
