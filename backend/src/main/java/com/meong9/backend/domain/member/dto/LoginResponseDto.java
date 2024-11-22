package com.meong9.backend.domain.member.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class LoginResponseDto {
    private Long memberId;
    private String email;
    private String nickname;
    private boolean isNewMember;
}
