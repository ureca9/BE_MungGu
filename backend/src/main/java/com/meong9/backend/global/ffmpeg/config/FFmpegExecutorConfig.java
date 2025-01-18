package com.meong9.backend.global.ffmpeg.config;

import lombok.RequiredArgsConstructor;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FFmpegExecutorConfig {
    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;

    @Bean
    public FFmpegExecutor ffmpegExecutor() {
        return new FFmpegExecutor(ffmpeg, ffprobe);
    }
}
