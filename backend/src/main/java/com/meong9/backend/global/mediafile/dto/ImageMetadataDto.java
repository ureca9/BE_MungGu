package com.meong9.backend.global.mediafile.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImageMetadataDto {
    private final int width;
    private final int height;
    private final long fileSize;
}
