package com.meong9.backend.global.mediafile.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.meong9.backend.global.mediafile.dto.S3UploadResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/****
 *  /추후에 MediaFileService로 통합해야함
 *
 */
@Service
@RequiredArgsConstructor
public class VideoService { // 추후에 ME
    private final AmazonS3Client s3Client;

    @Value("${s3.buckets.source}")
    private String bucket;

    /**
     * 파일 업로드 (이미지 또는 동영상)
     */
    public S3UploadResultDto uploadFile(MultipartFile file, Long memberId, String prefix, String suffix) throws IOException {
        String contentType = file.getContentType();

        if (contentType == null) {
            throw new IllegalArgumentException("파일 타입은 null이 아닙니다.");
        }
//        if (contentType.startsWith("image/")) {
//            return uploadImage(file, memberId, prefix, suffix);
//        } else if (contentType.startsWith("video/")) {
//            return uploadVideo(file, memberId, prefix, suffix);
//        } else {
//            throw new IllegalArgumentException("Unsupported file type: " + contentType);
//        }

        return uploadVideo(file, memberId, prefix, suffix);
    }

    /**
     * 동영상 업로드
     */
    private S3UploadResultDto uploadVideo(MultipartFile video, Long memberId, String prefix, String suffix) throws IOException {
        validateVideo(video);

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(video.getContentType());
        metadata.setContentLength(video.getSize());

        String fileKey = prefix + memberId + suffix;

        s3Client.putObject(bucket, fileKey, video.getInputStream(), metadata);

        String s3Url = s3Client.getUrl(bucket, fileKey).toString();
        return new S3UploadResultDto(s3Url, fileKey);
    }

    private static void validateVideo(MultipartFile video) {
        String originalFilename = video.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("허용되지 않은 비디오 포맷입니다");
        }

        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        if (!List.of("mp4", "avi", "mov", "mkv").contains(fileExtension)) {
            throw new IllegalArgumentException("지원되지 않는 형식: " + fileExtension);
        }

        if (video.getSize() > 500 * 1024 * 1024) { // 500MB 제한
            throw new IllegalArgumentException("동영상 사이즈는 500MB 이하여야 합니다.");
        }
    }

    /**
     * S3에서 파일 삭제
     */
    public void deleteFromS3(String fileKey) {
        if (s3Client.doesObjectExist(bucket, fileKey)) {
            s3Client.deleteObject(bucket, fileKey);
        } else {
            throw new IllegalArgumentException("Invalid file key: " + fileKey);
        }
    }
}
