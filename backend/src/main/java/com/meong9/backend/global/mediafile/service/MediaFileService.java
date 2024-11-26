package com.meong9.backend.global.mediafile.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.meong9.backend.global.exception.BadRequestException;
import com.meong9.backend.global.mediafile.dto.ImageMetadataDto;
import com.meong9.backend.global.mediafile.dto.S3UploadResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    public S3UploadResultDto uploadProfileImage(MultipartFile image, Long memberId) throws IOException {
        validateImage(image);

        // 이미지 변환 처리 (PNG -> JPG)
        BufferedImage originalImage = ImageIO.read(image.getInputStream());
        BufferedImage rgbImage = convertToRgbImage(originalImage);

        return uploadImageToS3(memberId, rgbImage);
    }

    /**
     * 카카오에서 받은 이미지 URL로 S3에 업로드
     */
    public S3UploadResultDto uploadFromUrl(String imageUrl, Long memberId) throws IOException {
        // URL에서 이미지 다운로드
        URL url = new URL(imageUrl);
        BufferedImage bufferedImage = ImageIO.read(url);

        return uploadImageToS3(memberId, bufferedImage);
    }

    private S3UploadResultDto uploadImageToS3(Long memberId, BufferedImage rgbImage) throws IOException {
        // 1. 이미지 메타데이터 추출
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(rgbImage, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();

        // 2. S3 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/jpeg");
        metadata.setContentLength(imageBytes.length);

        // 3. S3 파일 키 생성
        String fileKey = "Mprofile/" + memberId + "_profile.jpg";

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
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        if (!List.of("jpg", "jpeg", "png").contains(fileExtension)) {
            throw BadRequestException.invalidImageFormat();
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

    public void deleteProfileImage(Long memberId) {
        String fileKey = "Mprofile/" + memberId + "_profile.jpg";
        s3Client.deleteObject(bucket, fileKey);
    }


    /**
     * S3에 파일 업로드 및 URL 반환 (사용자 지정 키 사용)
     */
    public String uploadToS3WithCustomKey(MultipartFile image, String fileKey) throws IOException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(image.getContentType());
        metadata.setContentLength(image.getSize());

        s3Client.putObject(bucket, fileKey, image.getInputStream(), metadata);

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

    // S3에서 파일 삭제
    public void deleteFromS3(String fileKey) {
        if (s3Client.doesObjectExist(bucket, fileKey)) {
            s3Client.deleteObject(bucket, fileKey);
        } else {
            throw new IllegalArgumentException("S3에 파일이 존재하지 않습니다: " + fileKey);
        }
    }

}
