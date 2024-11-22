package com.meong9.backend.global.mediafile.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class S3UploadResultDto {
    private final String s3Url;
    private final String fileKey;
}
