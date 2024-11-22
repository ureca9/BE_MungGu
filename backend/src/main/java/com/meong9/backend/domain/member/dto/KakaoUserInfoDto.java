package com.meong9.backend.domain.member.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KakaoUserInfoDto {
    private String id;
    private String nickname;
    private String email;
    private String profileImageUrl;

}
