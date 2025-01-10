package com.meong9.backend.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class PresignedUrlDto {
    private String fileName;
    private String url;
}
