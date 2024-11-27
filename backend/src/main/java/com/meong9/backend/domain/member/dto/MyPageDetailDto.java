package com.meong9.backend.domain.member.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MyPageDetailDto {
    private final String email;
    private final String name;
    private final String nickname;
    private final String phone;
    private final String profileImageUrl;

    @Builder
    public MyPageDetailDto(String email, String name, String nickname, String phone, String profileImageUrl) {
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.phone = phone;
        this.profileImageUrl = profileImageUrl;
    }
}
