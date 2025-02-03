package com.meong9.backend.domain.review.dto;

import com.meong9.backend.domain.review.entity.ReviewFile;
import com.meong9.backend.global.mediafile.entity.FileType;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FileResponseDto {
    private Long reviewId;
    private FileType fileType;
    private Integer fileSize;
    private String fileUrl;
    private String fileName;

    public FileResponseDto(ReviewFile reviewFile) {
        MediaFile file = reviewFile.getFile();
        this.fileType = file.getFileType();
        this.fileSize = file.getFileSize();
        this.fileUrl = file.getFileUrl();
        this.fileName = file.getFileName();
    }

    public FileResponseDto(Long reviewId, FileType fileType, Integer fileSize, String fileUrl, String fileName) {
        this.reviewId = reviewId;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
    }
}
