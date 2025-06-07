package com.meong9.backend.global.mediafile.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
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
import com.meong9.backend.global.mediafile.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class MediaFileService {

    private final AmazonS3Client s3Client;
    private final MediaFileRepository mediaFileRepository;
    private final AmazonS3 amazonS3;
    private final TempStorageService tempStorageService;
    private FileUtil fileUtil;

    @Qualifier("videoTaskExecutor")
    private final ThreadPoolTaskExecutor taskExecutor;

    @Value("${s3.buckets.source}")
    private String bucket;

    @Value("${s3.buckets.resize}")
    private String resizeBucket;

    @Value("${s3.credentials.region}")
    private String region;


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
        if (!List.of("jpg", "jpeg", "png", "webp", "mp4", "mov").contains(fileExtension)) {
            throw BadRequestException.invalidImageVideoFormat();
        }

        // Webp 이미지인 경우 변환
        if (fileExtension.equals("webp")) {
            try {
                Class.forName("com.luciad.imageio.webp.WebPReadParam");
            } catch (ClassNotFoundException e) {
                throw BadRequestException.invalidImageVideoFormat();
            }
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

    /**
     * byte[] 데이터에서 이미지 메타데이터 추출
     */
    public ImageMetadataDto extractImageMetadata(byte[] imageData) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(imageData)) {
            BufferedImage bufferedImage = ImageIO.read(inputStream);

            int width = bufferedImage.getWidth();
            int height = bufferedImage.getHeight();

            return new ImageMetadataDto(width, height, imageData.length);
        }
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
     * 이미지·동영상 업로드 & 메타정보 저장.
     * - 동영상: H.265 2-pass, CRF20, scale=1280:-2
     * - 이미지: WebP, q=85, scale=1024:-2
     */
    @Retryable(
            value = { IOException.class, TimeoutException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public MediaFile handleMedia(MultipartFile file,
                                 Long reviewId,
                                 AtomicInteger fileNum)
            throws IOException, InterruptedException, TimeoutException {

        validateReviewFile(file);
        String baseKey = "review/" + reviewId + "/" + fileNum.getAndIncrement();
        String contentType = file.getContentType();

        // 로컬 임시 입력 저장
        Path tmpIn = Files.createTempFile("orig-", "-" + file.getOriginalFilename());
        try (InputStream is = file.getInputStream()) {
            Files.copy(is, tmpIn, StandardCopyOption.REPLACE_EXISTING);
        }

        if (contentType != null && contentType.startsWith("video/")) {
            // ——— 동영상 2-Pass H.265 인코딩 ———
            Path stats = Files.createTempFile("ffmpeg-passlog-", ".log");
            Path tmpOut = Files.createTempFile("enc-", ".mp4");

            // 1st pass
            ProcessBuilder pb1 = new ProcessBuilder(
                    "ffmpeg", "-y",
                    "-i", tmpIn.toString(),
                    "-c:v", "libx265", "-preset", "medium",
                    "-x265-params", "crf=20:pass=1:stats="+ stats,
                    "-an", "-f", "null", "/dev/null"
            ).redirectErrorStream(true);
            Process p1 = pb1.start();
            if (!p1.waitFor(60, TimeUnit.SECONDS) || p1.exitValue() != 0) {
                throw new IOException("FFmpeg 1st-pass 실패");
            }

            // 2번째 단계
            ProcessBuilder pb2 = new ProcessBuilder(
                    "ffmpeg", "-y",
                    "-i", tmpIn.toString(),
                    "-c:v", "libx265", "-preset", "medium",
                    "-x265-params", "crf=20:pass=2:stats="+ stats,
                    "-c:a", "aac", "-b:a", "128k",
                    "-vf", "scale=1280:-2",
                    tmpOut.toString()
            ).redirectErrorStream(true);
            Process p2 = pb2.start();
            if (!p2.waitFor(120, TimeUnit.SECONDS) || p2.exitValue() != 0) {
                throw new IOException("FFmpeg 2nd-pass 실패");
            }

            // S3 업로드
            String videoKey = baseKey + "_h265.mp4";
            S3UploadResultDto upload = uploadToS3(tmpOut.toFile(), videoKey, "video/mp4");

            // 메타정보 추출
            VideoMetaDataDto vm = fileUtil.extractVideoMetadata(tmpOut.toFile());

            // DB 저장
            MediaFile saved = mediaFileRepository.save(MediaFile.builder()
                    .fileType(FileType.VIDEO)
                    .fileName(file.getOriginalFilename())
                    .fileKey(upload.getFileKey())
                    .fileUrl(upload.getS3Url())
                    .fileSize((int) tmpOut.toFile().length())
                    .width(vm.getWidth().doubleValue())
                    .height(vm.getHeight().doubleValue())
                    .build()
            );

            // 임시 파일 정리
            Files.deleteIfExists(tmpIn);
            Files.deleteIfExists(tmpOut);
            Files.deleteIfExists(stats);
            return saved;

        } else if (contentType != null && contentType.startsWith("image/")) {
            // 이미지 -> WebP 변환
            Path tmpOut = Files.createTempFile("conv-", ".webp");
            ProcessBuilder pb = new ProcessBuilder(
                    "ffmpeg", "-y",
                    "-i", tmpIn.toString(),
                    "-vf", "scale=1024:-2",
                    "-q:v", "85",
                    tmpOut.toString()
            ).redirectErrorStream(true);
            Process proc = pb.start();
            if (!proc.waitFor(30, TimeUnit.SECONDS) || proc.exitValue() != 0) {
                throw new IOException("FFmpeg WebP 변환 실패");
            }

            // S3 업로드
            String imgKey = baseKey + ".webp";
            S3UploadResultDto upload = uploadToS3(tmpOut.toFile(), imgKey, "image/webp");

            // 메타정보 추출
            BufferedImage img = ImageIO.read(tmpOut.toFile());
            int w = img.getWidth(), h = img.getHeight();
            long size = Files.size(tmpOut);

            // DB 저장
            MediaFile saved = mediaFileRepository.save(MediaFile.builder()
                    .fileType(FileType.IMAGE)
                    .fileName(file.getOriginalFilename())
                    .fileKey(upload.getFileKey())
                    .fileUrl(upload.getS3Url())
                    .fileSize((int) size)
                    .width((double) w)
                    .height((double) h)
                    .build()
            );

            Files.deleteIfExists(tmpIn);
            Files.deleteIfExists(tmpOut);
            return saved;

        } else {
            Files.deleteIfExists(tmpIn);
            throw BadRequestException.invalidImageVideoFormat();
        }
    }

    // 3회 재시도 후에도 실패하면 호출—실패 정보 기록
    @Recover
    public void recoverMedia(Exception e,
                             MultipartFile file,
                             Long reviewId,
                             AtomicInteger fileNum) {
        tempStorageService.recordFailure(
                reviewId,
                file.getOriginalFilename(),
                LocalDateTime.now(),
                e.getMessage()
        );
    }

    private void validateReviewFile(MultipartFile file) {
        String name = Objects.requireNonNull(file.getOriginalFilename());
        if (!name.contains(".")) {
            throw BadRequestException.invalidImageVideoFormat();
        }
    }

    private S3UploadResultDto uploadToS3(File f,
                                         String key,
                                         String contentType) throws IOException {
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentType(contentType);
        meta.setContentLength(f.length());
        try (InputStream in = new FileInputStream(f)) {
            s3Client.putObject(bucket, key, in, meta);
        }
        return new S3UploadResultDto(
                s3Client.getUrl(bucket, key).toExternalForm(),
                key
        );
    }

}