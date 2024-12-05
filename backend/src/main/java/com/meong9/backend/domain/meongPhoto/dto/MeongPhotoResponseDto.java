package com.meong9.backend.domain.meongPhoto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeongPhotoResponseDto {

    private final String imageUrl;
    private final String imageDownloadUrl;
}
