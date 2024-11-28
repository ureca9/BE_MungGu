package com.meong9.backend.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryResponseDto { // 이전: ReviewDto
    private Long reviewId;
    private String profileImageUrl;
    private String content;
    private Double score;
    private String visitDate;
    private String createdAt;
    private String modifiedAt;
    private String nickname;
    private List<FileResponseDto> file;

}
