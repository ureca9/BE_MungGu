package com.meong9.backend.domain.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MypagePuppyDto {
    private final Long puppyId;
    private final String puppyName;
    private final String puppyImageUrl;

    @Builder
    public MypagePuppyDto(Long puppyId, String puppyName, String puppyImageUrl) {
        this.puppyId = puppyId;
        this.puppyName = puppyName;
        this.puppyImageUrl = puppyImageUrl;
    }
}
