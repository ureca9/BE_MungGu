package com.meong9.backend.domain.alarm.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FcmRequestDto {
    private String targetToken;
    private String title;
    private String body;
}