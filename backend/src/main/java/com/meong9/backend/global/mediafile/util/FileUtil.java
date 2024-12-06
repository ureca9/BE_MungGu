package com.meong9.backend.global.mediafile.util;

import com.meong9.backend.global.mediafile.dto.VideoMetaDataDto;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileUtil {
    public VideoMetaDataDto extractVideoMetadata(File videoFile) throws IOException, InterruptedException {
        // FFprobe 명령 설정
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffprobe",
                "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=width,height,duration",
                "-of", "csv=p=0",
                videoFile.getAbsolutePath()
        );

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        // 출력 파싱
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line = reader.readLine();
            process.waitFor();
            if (line != null) {
                String[] metadata = line.split(",");
                Integer width = Integer.parseInt(metadata[0]);
                Integer height = Integer.parseInt(metadata[1]);
                Double duration = Double.parseDouble(metadata[2]);
                return new VideoMetaDataDto(duration, width, height);
            }
        }
        throw new IllegalArgumentException("파일 메타데이터 추출에 실패했습니다.");
    }

}
