package com.meong9.backend.domain.review.dto;

import com.meong9.backend.domain.review.entity.ReviewFile;
import lombok.Builder;
import lombok.Getter;

/**
 * ReviewSummaryResponseDto의 File을 담기 위한 Dto
 */
@Getter
@Builder
public class ReviewSummaryFileDto {
    private final Long mediaFileId;
    private final String fileType;
    private final String fileUrl;

    /**
     * ReviewFile 엔티티를 ReviewSummaryFileDto로 변환하는 메서드
     *
     * @param reviewFile ReviewFile 엔티티 객체
     * @return 변환된 ReviewSummaryFileDto 객체
     */
    public static ReviewSummaryFileDto from(ReviewFile reviewFile) {
        if (reviewFile == null || reviewFile.getFile() == null) {
            throw new IllegalArgumentException("ReviewFile 또는 File 객체가 null입니다");
            }
        return ReviewSummaryFileDto.builder()
                .mediaFileId(reviewFile.getId().getMediaFileId()) // ReviewFile ID
                .fileType(String.valueOf(reviewFile.getFile().getFileType()))     // 파일 타입
                .fileUrl(reviewFile.getFile().getFileUrl())       // 파일 URL
                .build();
    }
}
