package com.meong9.backend.global.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meong9.backend.domain.review.entity.ReviewFile;
import com.meong9.backend.domain.review.repository.ReviewFileRepository;
import com.meong9.backend.global.exception.NotFoundException;
import com.meong9.backend.global.kafka.dto.VideoMessage;
import com.meong9.backend.global.kafka.service.OutboxService;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import com.meong9.backend.global.mediafile.repository.MediaFileRepository;
import com.meong9.backend.global.mediafile.service.MediaFileService;
import com.meong9.backend.global.slack.service.SlackNotificationService;
import com.meong9.backend.global.slack.entity.SlackNotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoProcessingConsumer {

    private final MediaFileService mediaFileService;
    private final MediaFileRepository mediaFileRepository;
    private final FFmpegExecutor ffmpegExecutor;
    private final ReviewFileRepository reviewFileRepository;
    private final OutboxService outboxService;
    private final SlackNotificationService slackNotificationService;

    @Value("${s3.buckets.source}")
    private String bucket;

    @RetryableTopic(
            // default 3번 재시도
            backoff = @Backoff(delay = 5000), // 재시도 간격 (5초)
            autoCreateTopics = "false" // 토픽 자동 생성 방지
    )
    @KafkaListener(topics = "video-transcoding", groupId = "video-transcoding-group", concurrency = "2")
    @Transactional
    public void listen(String message) {
        log.info("Kafka 메시지 수신: {}", message);
        try {
            VideoMessage videoMessage = parseMessage(message);
            processVideo(videoMessage);
            log.info("비디오 처리 완료: {}", videoMessage.getFileUrl());
        } catch (Exception e) {
            log.error("비디오 처리 실패: {}", message, e);
            throw e;
        }
    }

    private VideoMessage parseMessage(String message) {
        try {
            return new ObjectMapper().readValue(message, VideoMessage.class);
        } catch (Exception e) {
            throw new RuntimeException("카프카 메시지 파싱 중 에러 발생: " + message, e);
        }
    }

    private void processVideo(VideoMessage videoMessage) {
        String fileUrl = videoMessage.getFileUrl();
        // 1. 비디오 파일 검증 및 가져오기
        MediaFile mediaFile = mediaFileRepository.findByFileUrl(fileUrl)
                .orElseThrow(() -> NotFoundException.entityNotFound(String.format("media file url - %s", fileUrl)));
        ReviewFile reviewFile = reviewFileRepository.findByMediaFileId(mediaFile.getMediaFileId())
                .orElseThrow(() -> NotFoundException.entityNotFound(String.format("review file - %s", mediaFile.getMediaFileId())));

        try {
            // 2. 트랜스 코딩
            String outputDirPath = transCodeToHls(fileUrl);

            // 3. S3 업로드
            String s3Directory = "Review/" + extractFileNameWithoutExtension(fileUrl) + "_hls";
            mediaFileService.uploadHlsToS3(outputDirPath, s3Directory);

            // 4. MediaFile, ReviewFile 상태 업데이트
            String s3BaseUrl = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com";
            mediaFile.setFileUrl(s3BaseUrl + "/" + s3Directory + "/master.m3u8"); // HLS 마스터 플레이리스트 경로
            reviewFile.setStatus("TRANSCODED");

            // 5. 영상 원본 및 로컬 디렉토리 삭제
            String fileKey = extractFileKey(fileUrl);
            mediaFileService.deleteFromS3(fileKey);
            deleteLocalDirectory(outputDirPath);
        } catch (Exception e) {
            // 트랜스코딩 실패 시 상태 업데이트
            handleFailedMessage(mediaFile.getMediaFileId(), reviewFile);
            throw new RuntimeException("비디오 처리 중 오류 발생: " + e.getMessage(), e);
        }
    }

    private void handleFailedMessage(Long mediaFileId, ReviewFile reviewFile) {
        reviewFile.setStatus("FAILED");
        reviewFileRepository.save(reviewFile);

        // Outbox 상태 관리
        outboxService.updateOutbox(mediaFileId);

        // Slack 알림 전송
        String slackMessage = String.format("트랜스 코딩 실패 - MediaFile ID: %d, ReviewFile: %s", mediaFileId, reviewFile.getId());
        slackNotificationService.sendSlackNotification(SlackNotificationType.KAFKA, slackMessage);

    }

    public String transCodeToHls(String fileUrl) {
        // 1. URL에서 파일 이름 추출
        String fileName = extractFileNameWithoutExtension(fileUrl);
        String outputDirPath = String.format("/tmp/%s_hls", fileName);
        File outputDir = new File(outputDirPath);

        // 디렉토리 생성 및 확인
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new RuntimeException("HLS 파일 저장 디렉토리 생성 실패: " + outputDirPath);
        }

        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(fileUrl)
                .addExtraArgs("-y")
                .addOutput(outputDirPath + "/master_%v.m3u8") // 각 해상도별 마스터 파일
                .setFormat("hls")
                .addExtraArgs("-hls_time", "4")
                .addExtraArgs("-hls_playlist_type", "vod")
                .addExtraArgs("-hls_segment_filename", outputDirPath + "/fileSequence_%v_%03d.ts") // 세그먼트 파일 이름
                .addExtraArgs("-var_stream_map", "v:0,name:360p v:1,name:720p") // 스트림 매핑
                .addExtraArgs("-master_pl_name", "master.m3u8") // 전체 플레이리스트 파일

                // 360p 설정
                .addExtraArgs("-map", "0:v:0")
                .addExtraArgs("-b:v:2", "1000k")
                .addExtraArgs("-maxrate:v:2", "1000k")
                .addExtraArgs("-bufsize:v:2", "2000k")
                .addExtraArgs("-s:v:2", "854x480")
                .addExtraArgs("-crf:v:2", "28")
                .addExtraArgs("-b:a:2", "64k")

                // 720p 설정
                .addExtraArgs("-map", "0:v:0")
                .addExtraArgs("-b:v:1", "2500k")
                .addExtraArgs("-maxrate:v:1", "2500k")
                .addExtraArgs("-bufsize:v:1", "5000k")
                .addExtraArgs("-s:v:1", "1280x720")
                .addExtraArgs("-crf:v:1", "22")
                .addExtraArgs("-b:a:1", "96k")

                .done();

        ffmpegExecutor.createJob(builder).run();
        log.info("HLS 트랜스코딩 완료: " + outputDirPath);

        return outputDirPath;
    }

    /**
     * 로컬 디렉토리를 삭제
     */
    private void deleteLocalDirectory(String directoryPath) {
        File dir = new File(directoryPath);
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (!file.delete()) {
                    System.err.println("파일 삭제 실패: " + file.getAbsolutePath());
                }
            }
        }
        if (!dir.delete()) {
            System.err.println("디렉토리 삭제 실패: " + directoryPath);
        } else {
            System.out.println("로컬 디렉토리 삭제 완료: " + directoryPath);
        }
    }

    private String extractFileNameWithoutExtension(String fileUrl) {
        String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        return fileName.replaceAll("\\.[^.]+$", ""); // 확장자 제거
    }

    private String extractFileKey(String fileUrl) throws MalformedURLException {
        URL url = new URL(fileUrl);
        return url.getPath().substring(1);
    }

}
