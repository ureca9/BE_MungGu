package com.meong9.backend.global.mediafile.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.meong9.backend.global.exception.BadRequestException;
import com.meong9.backend.global.mediafile.dto.ImageMetadataDto;
import com.meong9.backend.global.mediafile.dto.S3UploadResultDto;
import com.meong9.backend.global.mediafile.dto.VideoMetaDataDto;
import com.meong9.backend.global.mediafile.entity.FileType;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import com.meong9.backend.global.mediafile.repository.MediaFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class MediaFileService {

    private final AmazonS3Client s3Client;
    private final MediaFileRepository mediaFileRepository;
    @Qualifier("taskExecutor")
    private final ThreadPoolTaskExecutor taskExecutor;


    @Value("${s3.bucket}")
    private String bucket;

    /**
     * 유저가 등록한 프로필 이미지를 S3에 업로드
     */
    public S3UploadResultDto uploadProfileImage(MultipartFile image, Long memberId, String prefix, String suffix) throws IOException {
        validateFile(image);

        // 이미지 변환 처리 (PNG -> JPG)
        BufferedImage originalImage = ImageIO.read(image.getInputStream());
        BufferedImage rgbImage = convertToRgbImage(originalImage);

        return uploadImageToS3(memberId, rgbImage,prefix,suffix);
    }

    /**
     * 카카오에서 받은 이미지 URL로 S3에 업로드
     */
    public S3UploadResultDto uploadFromUrl(String imageUrl, Long memberId, String prefix, String suffix) throws IOException {
        // URL에서 이미지 다운로드
        URL url = new URL(imageUrl);
        BufferedImage bufferedImage = ImageIO.read(url);

        return uploadImageToS3(memberId, bufferedImage,prefix,suffix);
    }

    /**
     * 유저가 등록한 멍생네컷 이미지를 S3에 업로드
     */
    public S3UploadResultDto uploadMeongPhoto(MultipartFile image, Long memberId, String prefix,  String suffix) throws IOException {
        validateFile(image);

        // 이미지 변환 처리 (PNG -> JPG)
        BufferedImage originalImage = ImageIO.read(image.getInputStream());
        BufferedImage rgbImage = convertToRgbImage(originalImage);

        return uploadImageToS3(memberId, rgbImage,prefix,suffix);
    }

    /**
     * S3에 이미지 업로드
     */
    public S3UploadResultDto uploadImageToS3(Long memberId, BufferedImage rgbImage,String prefix,String suffix) throws IOException {
        // 1. 이미지 메타데이터 추출
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(rgbImage, "jpg", baos);
        byte[] imageBytes = baos.toByteArray();

        // 2. S3 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/jpeg");
        metadata.setContentLength(imageBytes.length);

        // 3. S3 파일 키 생성
        String fileKey = prefix + memberId + suffix;

        // 4. S3 업로드
        ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
        s3Client.putObject(bucket, fileKey, inputStream, metadata);

        // 5. S3 URL 생성 및 파일 키 반환
        String s3Url = s3Client.getUrl(bucket, fileKey).toString();
        return new S3UploadResultDto(s3Url, fileKey);
    }

    private static void validateFile(MultipartFile image) {
        String originalFilename = image.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw BadRequestException.invalidImageVideoFormat();
        }

        // 파일 확장자 추출 후 소문자로 변환
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();

        // 허용 확장자를 소문자로 비교
        if (!List.of("jpg", "jpeg", "png","mp4","mov").contains(fileExtension)) {
            throw BadRequestException.invalidImageVideoFormat();
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
    public String uploadToS3WithCustomKey(MultipartFile file, String fileKey) throws IOException {
        // 파일 유효성 검증
        validateFile(file);

        // S3 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        // S3에 파일 업로드
        s3Client.putObject(bucket, fileKey, file.getInputStream(), metadata);

        // S3 URL 반환
        return s3Client.getUrl(bucket, fileKey).toString();
    }

    /**
     * S3 이미지 다운로드 URL 반환
     */
    public String generateDownloadUrl(String s3Url, int hour) throws MalformedURLException {
        URL url = new URL(s3Url);
        String key = url.getPath().substring(1);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, key)
                .withMethod(HttpMethod.GET)
                .withExpiration(Date.from(Instant.now().plus(hour, ChronoUnit.HOURS)));

        // 다운로드 Url을 입력하면 바로 다운로드 되게끔 강제하는 설정
        request.addRequestParameter(
                "response-content-disposition",
                "attachment; filename=\"" + key.substring(key.lastIndexOf("/") + 1) + "\""
        );

        URL downloadUrl = s3Client.generatePresignedUrl(request);
        return downloadUrl.toString();
    }

    /**
     * 미디어 파일 저장
     */
    public MediaFile saveMediaFile(ImageMetadataDto metadata, S3UploadResultDto s3UploadResultDto) {
        MediaFile mediaFile = MediaFile.builder()
                .fileType(FileType.IMAGE)
                .fileSize((int) metadata.getFileSize())
                .fileName("meongPhoto.jpg")
                .fileUrl(s3UploadResultDto.getS3Url())
                .height((double) metadata.getHeight())
                .width((double) metadata.getWidth())
                .fileKey(s3UploadResultDto.getFileKey())
                .build();

        return mediaFileRepository.save(mediaFile);
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
            throw BadRequestException.invalidFilekeyFormat(fileKey);
        }
    }

    public MediaFile createDefaultProfileImage() {
        return MediaFile.builder()
                .fileType(FileType.IMAGE)
                .fileName("default_profile.png")
                .fileKey("Mprofile/default_profile.png")
                .fileUrl("https://uplus-s3-bucket-1.s3.ap-northeast-2.amazonaws.com/Mprofile/default_profile.png")
                .build();
    }

    /**
     * MultipartFile에서 이미지 메타데이터 추출
     */
    public VideoMetaDataDto extractVideoMetadata(MultipartFile video) throws IOException, InterruptedException, TimeoutException {
        // 타임아웃 설정 (초 단위)
        int timeout = 30;

        // FFprobe 명령어 설정
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffprobe",
                "-v", "error", // 에러 메시지 최소화
                "-select_streams", "v:0", // 비디오 스트림만 선택
                "-show_entries", "stream=width,height,duration", // 필요한 메타데이터 필드 지정
                "-of", "csv=p=0", // CSV 형식 출력
                "pipe:0" // 입력 데이터를 파이프로 전달
        );

        Process process = null; // 프로세스 객체 선언
        try {
            // FFprobe 프로세스 시작
            process = processBuilder.start();

            // CompletableFuture를 사용하여 비동기 작업 수행
            CompletableFuture<String> future = executeWithTimeout(process, video);

            // 결과 가져오기 (타임아웃 적용)
            String result = future.get(timeout, TimeUnit.SECONDS);

            // FFprobe 출력 결과를 파싱하여 메타데이터 DTO로 변환
            return parseMetadata(result);
        } catch (TimeoutException e) {
            throw new TimeoutException("비디오 메타데이터 추출 시간 초과");
        } catch (Exception e) {
            throw new IOException("비디오 메타데이터 추출 실패: " + e.getMessage());
        } finally {
            // 프로세스 종료 (강제 종료 포함)
            if (process != null) {
                process.destroyForcibly();
            }
        }
    }

    /**
     * FFprobe와 MultipartFile 연결 및 결과 반환 (비동기 방식)
     */
    private CompletableFuture<String> executeWithTimeout(Process process, MultipartFile video) {
        return CompletableFuture.supplyAsync(() -> {
            try (
                    // FFprobe 입력 스트림과 출력 스트림 연결
                    OutputStream stdin = process.getOutputStream();
                    InputStream videoStream = video.getInputStream();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream())
                    )
            ) {
                // MultipartFile 데이터를 FFprobe의 stdin으로 전달
                videoStream.transferTo(stdin);
                stdin.close(); // 입력 종료

                // FFprobe 출력 결과를 읽어 반환
                return reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException("FFprobe 실행 중 오류 발생", e);
            }
        }, taskExecutor);
    }

    /**
     * FFprobe 출력 데이터를 파싱하여 VideoMetaDataDto로 변환
     */
    private VideoMetaDataDto parseMetadata(String line) {
        if (line == null || line.trim().isEmpty()) {
            throw new IllegalArgumentException("메타데이터가 비어있습니다");
        }
        String[] parts = line.split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException("잘못된 메타데이터 형식");
        }
        return new VideoMetaDataDto(
                Double.parseDouble(parts[2]), // duration (초)
                Integer.parseInt(parts[0]), // width (픽셀)
                Integer.parseInt(parts[1])  // height (픽셀)
        );
    }

}