package com.meong9.backend.domain.review.dto;

import com.meong9.backend.domain.review.entity.ReviewFile;
import com.meong9.backend.global.mediafile.entity.FileType;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import lombok.Builder;
import lombok.Getter;

@Getter
public class FileResponseDto {
    private final FileType fileType;
    private final Integer fileSize;
    private final String fileUrl;
    private final String fileName;

    public FileResponseDto(ReviewFile reviewFile) {
        MediaFile file = reviewFile.getFile();
        this.fileType = file.getFileType();
        this.fileSize = file.getFileSize();
        this.fileUrl = file.getFileUrl();
        this.fileName = file.getFileName();
    }

    public FileResponseDto(FileType fileType, Integer fileSize, String fileUrl, String fileName) {
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
    }
}
