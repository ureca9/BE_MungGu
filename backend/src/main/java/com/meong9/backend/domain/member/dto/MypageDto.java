package com.meong9.backend.domain.member.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class MypageDto {
    private final Long memberId;
    private final String nickname;
    private final String profileImageUrl;
    private final List<MypagePuppyDto> puppyList;

    @Builder
    public MypageDto(Long memberId, String nickname, String profileImageUrl, List<MypagePuppyDto> puppyList) {
        this.memberId = memberId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.puppyList = puppyList;
    }
}
