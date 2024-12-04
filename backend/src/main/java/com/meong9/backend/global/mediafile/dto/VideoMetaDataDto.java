package com.meong9.backend.global.mediafile.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VideoMetaDataDto {
    private final Double duration;
    private final Integer width;
    private final Integer height;
}
