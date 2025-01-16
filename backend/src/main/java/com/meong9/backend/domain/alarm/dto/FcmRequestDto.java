package com.meong9.backend.domain.alarm.dto;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmRequestDto {
    private Long memberId;
    private String token;
}