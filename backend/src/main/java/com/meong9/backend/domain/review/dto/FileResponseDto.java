package com.meong9.backend.domain.review.dto;

import com.meong9.backend.domain.review.entity.ReviewFile;
import com.meong9.backend.global.mediafile.entity.FileType;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FileResponseDto {
    private final FileType fileType;
    private final Integer fileSize;
    private final String fileUrl;
    private final String fileName;

    public static FileResponseDto from(ReviewFile reviewFile) {
        MediaFile file = reviewFile.getFile();
        return FileResponseDto.builder()
                .fileType(file.getFileType())
                .fileSize(file.getFileSize())
                .fileUrl(file.getFileUrl())
                .fileName(file.getFileName())
                .build();
    }
}
