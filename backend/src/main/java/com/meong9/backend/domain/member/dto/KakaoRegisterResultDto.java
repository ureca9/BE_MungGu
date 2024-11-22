package com.meong9.backend.domain.member.dto;

import com.meong9.backend.domain.member.entity.Member;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KakaoRegisterResultDto {
    private Member member;
    private boolean isNewMember;
}
