package com.meong9.backend.global.mediafile.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.meong9.backend.global.mediafile.dto.ImageMetadataDto;
import com.meong9.backend.global.mediafile.dto.S3UploadResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class MediaFileService {

    private final AmazonS3Client s3Client;

    @Value("${s3.bucket}")
    private String bucket;

    public String upload(MultipartFile image) throws IOException {
        // 1. 업로드할 파일의 이름을 변경
        String originalFileName = image.getOriginalFilename();
        String fileName = generateFileName(originalFileName);

        // 2. S3에 업로드할 파일의 메타데이터 생성
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(image.getContentType());
        metadata.setContentLength(image.getSize());

        // 3. S3에 업로드
        s3Client.putObject(bucket, fileName, image.getInputStream(), metadata);

        // 4. 업로드한 파일의 S3 url 주소 반환
        return s3Client.getUrl(bucket, fileName).toString();
    }

    /**
     * 카카오에서 받은 이미지 URL로 S3에 업로드
     */
    public S3UploadResultDto uploadFromUrl(String imageUrl, String nickname) throws IOException {
        // 1. URL에서 이미지 다운로드
        URL url = new URL(imageUrl);
        BufferedImage bufferedImage = ImageIO.read(url);

        // 2. 이미지 메타데이터 추출
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();

        // 3. S3 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/jpeg");
        metadata.setContentLength(imageBytes.length);

        // 4. 파일 키 생성
        String fileKey = "Mprofile/" + nickname + "_profile.jpg";

        // 5. S3 업로드
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
        s3Client.putObject(bucket, fileKey, inputStream, metadata);

        // 6. S3 URL 생성
        String s3Url = s3Client.getUrl(bucket, fileKey).toString();

        // 7. S3 URL과 파일 키 반환
        return new S3UploadResultDto(s3Url, fileKey);
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
     * 파일 이름과 생성 시간을 결합해 이름을 생성
     */
    private String generateFileName(String originalFileName) {
        /* 업로드할 파일의 이름을 변경하는 로직 */
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return originalFileName + "_" + LocalDateTime.now().format(formatter);
    }
}
