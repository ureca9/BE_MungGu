package com.meong9.backend.domain.review.dto;

import com.meong9.backend.domain.review.entity.Review;
import com.meong9.backend.domain.review.entity.ReviewFile;
import com.meong9.backend.global.mediafile.entity.FileType;
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
    private FileResponseDto file;

    @Setter
    private String plcPenName;

    public MyReviewResponseDto(Long reviewId, String content, Float score, LocalDate visitDate,
                               String type, Long plcPenId, String nickname, ReviewFile reviewFile) {
        this.reviewId = reviewId;
        this.content = content;
        this.score = score;
        this.visitDate = visitDate;
        this.type = type;
        this.plcPenId = plcPenId;
        this.nickname = nickname;
        if(reviewFile != null) this.file=new FileResponseDto(reviewFile);
    }
}
