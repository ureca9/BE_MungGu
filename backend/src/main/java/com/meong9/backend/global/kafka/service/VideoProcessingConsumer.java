package com.meong9.backend.global.kafka.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.meong9.backend.global.exception.BadRequestException;
import com.meong9.backend.global.kafka.entity.VideoMessage;
import com.meong9.backend.global.mediafile.service.MediaFileService;
import com.meong9.backend.global.mediafile.service.VideoService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class VideoProcessingConsumer {

    private final MediaFileService mediaFileService;
    private final VideoService videoService;

    public VideoProcessingConsumer(MediaFileService mediaFileService, VideoService videoService) {
        this.mediaFileService = mediaFileService;
        this.videoService = videoService;
    }

    @KafkaListener(topics = "video-transcoding", groupId = "video-transcoding-group", concurrency = "3")
    public void listen(String message) {
        // 1. 메시지 수신: groupId를 통해 여러 Consumer가 병렬로 메시지를 처리할 수 있음
        // t3.medium 인스턴스는 2개의 vCPU를 가지므로, 3개의 컨슈머가 적절한 병렬 처리 수준
        // 토픽은 메시지를 보관하는 공간이고, 그룹은 메시지 소비자를 관리하는 단위. 3명이서 모여 해당 팀에 떨어진 일을 나눠 하는 방식.
        System.out.println("메시지 수신 완료: " + message);
        try {
            // 받은 Json메세지를 파싱해서 VideoMessage dto로 만듦
            VideoMessage videoMessage = parseMessage(message);

            // video 인코딩 시작
            transcodeVideo(videoMessage.getFileUrl());

            System.out.println("비디오 인코딩 완료: " + videoMessage.getFileUrl());
        } catch (Exception e) {
            // 좀더 정확한 에러 처리가 되었으면 좋겠는데, 에러 코드는 뭐가 좋을지?
            System.err.println("비디오 인코딩 중 에러 발생: " + e.getMessage());
        }
    }

    private VideoMessage parseMessage(String message) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(message, VideoMessage.class);
        } catch (Exception e) {
            throw new RuntimeException("카프카 메시지 파싱 중 에러 발생: " + message, e);
        }
    }

    private void transcodeVideo(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            throw BadRequestException.invalidFileUrl();
        }

        int[] resolutions = {360, 720};

        for (int resolution : resolutions) {
            // URL에서 파일 이름 추출
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            String outputFilePath = String.format("/tmp/%s_%dp.mp4", fileName.replaceAll("\\.[^.]+$", ""), resolution);

            // FFmpeg 명령어 생성
            String command = String.format(
                    "ffmpeg -y -i %s -c:v libx264 -preset fast -vf scale=trunc(oh*a/2)*2:%d %s",
                    fileUrl, resolution, outputFilePath
            );

            System.out.println("명령어 실행 중: " + command);

            try {
                // FFmpeg 프로세스 실행
                Process process = Runtime.getRuntime().exec(command);

                // 에러 발생 시 ffmpeg 에러 로그 출력
                try (BufferedReader stdOut = new BufferedReader(new InputStreamReader(process.getInputStream()));
                     BufferedReader stdErr = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

                    String line;
                    while ((line = stdOut.readLine()) != null) {
                        System.out.println("FFmpeg STDOUT: " + line);
                    }
                    while ((line = stdErr.readLine()) != null) {
                        System.err.println("FFmpeg STDERR: " + line);
                    }
                }

                int exitCode = process.waitFor();

                // 인코딩이 성공적으로 수행되었을 경우
                if (exitCode == 0) {
                    System.out.println(resolution + "p 트랜스코딩 완료: " + outputFilePath);

                    // S3 업로드 경로 생성
                    String s3Key = String.format("Review/%s_%dp.mp4", fileName.replaceAll("\\.[^.]+$", ""), resolution);
                    uploadToS3(outputFilePath, s3Key);

                    // 썸네일 생성 및 업로드
                    String thumbnailFile = createThumbnail(outputFilePath);
                    String thumbnailS3Key = String.format("Review/%s_%dp_thumbnail.jpg", fileName.replaceAll("\\.[^.]+$", ""), resolution);
                    uploadToS3(thumbnailFile, thumbnailS3Key);
                } else {
                    throw new RuntimeException(resolution + "p 트랜스코딩 실패 (FFmpeg 오류)");
                }
            } catch (IOException e) {
                throw new RuntimeException("FFmpeg 명령어 실행 중 오류 발생: " + e.getMessage(), e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("트랜스코딩 프로세스가 중단되었습니다.", e);
            }
        }
    }

    /**
     * 트랜스코딩된 파일을 S3에 업로드
     */
    private void uploadToS3(String localFilePath, String s3Key) {
        mediaFileService.uploadVideoAndThumbnail(localFilePath, s3Key);
    }

    /**
     * 썸네일을 생성. 각 해상도마다 썸네일을 생성할 필요가 있는지 확인 필요
     */
    private String createThumbnail(String videoPath) {
        String thumbnailPath = videoPath.replaceFirst("(\\.[^.]+)$", "_thumbnail.jpg");
        String command = String.format("ffmpeg -i %s -ss 00:00:01.000 -vframes 1 %s", videoPath, thumbnailPath);
        try {
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("썸네일 생성 완료: " + thumbnailPath);
            } else {
                throw new RuntimeException("썸네일 생성 실패: " + thumbnailPath);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("썸네일 생성 중 오류 발생", e);
        }
        return thumbnailPath;
    }
}
