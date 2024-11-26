package com.meong9.backend.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberInfoDto {
    @NotBlank(message = "이름은 필수 입력값입니다.")
    private String name;
    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    private String nickname;
    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    private String phone;
}
