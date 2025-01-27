package com.meong9.backend.global.kafka.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VideoMessage {
    private String videoId;
    private String fileUrl;

}

