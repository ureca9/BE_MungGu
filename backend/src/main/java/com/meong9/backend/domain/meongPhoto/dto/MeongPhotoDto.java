package com.meong9.backend.domain.meongPhoto.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeongPhotoDto {

    private final Long photoId;
    private final String nickname;
    private final String profileImageUrl;
    private final String meongPhotoUrl;
    private String createdAt;
}
