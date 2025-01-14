package com.meong9.backend.global.kafka.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VideoMessage {
    private String videoId;
    private String fileUrl;

}

