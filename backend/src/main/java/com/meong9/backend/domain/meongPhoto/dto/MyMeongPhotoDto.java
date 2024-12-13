package com.meong9.backend.domain.meongPhoto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyMeongPhotoDto {
    private final Long photoId;
    private final String meongPhotoUrl;
    private String createdAt;
}
