package com.meong9.backend.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ReviewSummaryResponseDto의 File을 담기 위한 Dto
 */
@Getter
@Builder
public class ReviewSummaryFileDto {
    private final Long mediaFileId;
    private final String fileType;
    private final String fileUrl;
}
