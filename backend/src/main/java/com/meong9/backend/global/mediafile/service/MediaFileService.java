package com.meong9.backend.global.mediafile.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.meong9.backend.global.exception.BadRequestException;
import com.meong9.backend.global.mediafile.dto.ImageMetadataDto;
import com.meong9.backend.global.mediafile.dto.S3UploadResultDto;
import com.meong9.backend.global.mediafile.dto.VideoMetaDataDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaFileService {

    private final AmazonS3Client s3Client;

    @Value("${s3.bucket}")
    private String bucket;

    /**
     * 유저가 등록한 프로필 이미지를 S3에 업로드
     */
    public S3UploadResultDto uploadProfileImage(MultipartFile image, Long memberId,String prefix,String suffix) throws IOException {
        validateImage(image);

        // 이미지 변환 처리 (PNG -> JPG)
        BufferedImage originalImage = ImageIO.read(image.getInputStream());
        BufferedImage rgbImage = convertToRgbImage(originalImage);

        return uploadImageToS3(memberId, rgbImage,prefix,suffix);
    }

    /**
     * 카카오에서 받은 이미지 URL로 S3에 업로드
     */
    public S3UploadResultDto uploadFromUrl(String imageUrl, Long memberId,String prefix,String suffix) throws IOException {
        // URL에서 이미지 다운로드
        URL url = new URL(imageUrl);
        BufferedImage bufferedImage = ImageIO.read(url);

        return uploadImageToS3(memberId, bufferedImage,prefix,suffix);
    }

    private S3UploadResultDto uploadImageToS3(Long memberId, BufferedImage rgbImage,String prefix,String suffix) throws IOException {
        // 1. 이미지 메타데이터 추출
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(rgbImage, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();

        // 2. S3 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/jpeg");
        metadata.setContentLength(imageBytes.length);

        // 3. S3 파일 키 생성
//        String fileKey = "Mprofile/" + memberId + "_profile.jpg";
        String fileKey = prefix + memberId + suffix;

        // 4. S3 업로드
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
        s3Client.putObject(bucket, fileKey, inputStream, metadata);

        // 5. S3 URL 생성 및 파일 키 반환
        String s3Url = s3Client.getUrl(bucket, fileKey).toString();
        return new S3UploadResultDto(s3Url, fileKey);
    }

    private static void validateImage(MultipartFile image) {
        String originalFilename = image.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw BadRequestException.invalidImageFormat();
        }

        // 파일 확장자 추출 후 소문자로 변환
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();

        // 허용 확장자를 소문자로 비교
        if (!List.of("jpg", "jpeg", "png","mp4","mov").contains(fileExtension)) {
            throw BadRequestException.invalidImageFormat();
        }
    }

    private static void validateVideo(MultipartFile video) {
        String originalFilename = video.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw BadRequestException.invalidVideoFormat();
        }

        // 파일 확장자 추출 후 소문자로 변환
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();

        // 허용 확장자를 소문자로 비교
        if (!List.of("mp4", "mov").contains(fileExtension)) {
            throw BadRequestException.invalidVideoFormat();
        }
    }

    /**
     * URL에서 이미지의 메타데이터 추출
     */
    public ImageMetadataDto extractImageMetadataFromUrl(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        BufferedImage bufferedImage = ImageIO.read(url);

        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        long fileSize = url.openConnection().getContentLengthLong();

        return new ImageMetadataDto(width, height, fileSize);
    }

    /**
     * 그림 투명도 제거
     */
    private BufferedImage convertToRgbImage(BufferedImage originalImage) {
        // RGB 형식의 빈 이미지 생성
        BufferedImage rgbImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        // 기존 이미지를 새 RGB 이미지에 그리기 (투명도 제거)
        Graphics2D g2d = rgbImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, Color.WHITE, null); // 투명한 부분은 흰색으로
        g2d.dispose();

        return rgbImage;
    }

    public void deleteProfileImage(Long memberId,String prefix,String suffix) {
        String fileKey = prefix + memberId + suffix;
        s3Client.deleteObject(bucket, fileKey);
    }


    /**
     * S3에 파일 업로드 및 URL 반환 (사용자 지정 키 사용)
     * 업로드 전 파일 유효성 검증 및 메타데이터 설정 포함
     */
    public String uploadToS3WithCustomKey(MultipartFile image, String fileKey) throws IOException {
        // 파일 유효성 검증
        validateImage(image);

        // S3 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(image.getContentType());
        metadata.setContentLength(image.getSize());

        // S3에 파일 업로드
        s3Client.putObject(bucket, fileKey, image.getInputStream(), metadata);

        // S3 URL 반환
        return s3Client.getUrl(bucket, fileKey).toString();
    }



    /**
     * MultipartFile에서 이미지 메타데이터 추출
     */
    public ImageMetadataDto extractImageMetadata(MultipartFile image) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(image.getInputStream());
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        return new ImageMetadataDto(width, height, image.getSize());
    }

    /**
     * MultipartFile에서 동영상 메타데이터 추출
     */
    public VideoMetaDataDto extractVideoMetadata(File video) throws IOException, InterruptedException {
        // FFmpeg ffprobe 명령 설정
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffprobe",
                "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=width,height,duration",
                "-of", "default=noprint_wrappers=1",
                video.getAbsolutePath()
        );
        processBuilder.redirectErrorStream(true); // 오류를 표준 출력으로 리다이렉트

        // 프로세스 실행
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        Integer width = null, height = null;
        Double duration = null;

        // ffprobe 출력 파싱
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("width=")) {
                width = Integer.parseInt(line.split("=")[1]);
            } else if (line.startsWith("height=")) {
                height = Integer.parseInt(line.split("=")[1]);
            } else if (line.startsWith("duration=")) {
                duration = Double.parseDouble(line.split("=")[1]);
            }
        }
        process.waitFor(); // 프로세스 종료 대기

        // 값 검증
        if (width == null || height == null || duration == null) {
            throw new IllegalArgumentException("Failed to extract video metadata");
        }

        return new VideoMetaDataDto(duration, width, height);
    }


    // S3에서 파일 삭제
    public void deleteFromS3(String fileKey) {
        if (s3Client.doesObjectExist(bucket, fileKey)) {
            s3Client.deleteObject(bucket, fileKey);
        } else {
            throw BadRequestException.invalidFilekeyFormat(fileKey);
        }
    }

}
