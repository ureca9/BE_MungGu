package com.meong9.backend.global.tempFile.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.member.repository.MemberRepository;
import com.meong9.backend.domain.meongPhoto.entity.MeongPhoto;
import com.meong9.backend.domain.meongPhoto.repository.MeongPhotoRepository;
import com.meong9.backend.global.exception.NotFoundException;
import com.meong9.backend.global.mediafile.dto.ImageMetadataDto;
import com.meong9.backend.global.mediafile.dto.S3UploadResultDto;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import com.meong9.backend.global.mediafile.service.MediaFileService;
import com.meong9.backend.global.tempFile.entity.TempFile;
import com.meong9.backend.global.tempFile.repository.TempFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "TempFileService")
public class TempFileService {

    private final TempFileRepository tempFileRepository;
    private final AmazonS3Client s3Client;
    private final MediaFileService mediaFileService;
    private final MemberRepository memberRepository;
    private final MeongPhotoRepository meongPhotoRepository;

    @Value("${s3.bucket}")
    private String bucket;

    /**
     * 바이너리 파일을 임시 테이블에 저장
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveTemporaryFile(String serviceUrl, MultipartFile file, Long id, String type) {
        try {
            TempFile temporaryFile = new TempFile(
                    serviceUrl,
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes(),
                    id,
                    type
            );
            tempFileRepository.save(temporaryFile);
        } catch (IOException e) {
            log.error("임시 파일 저장 중 오류 발생. 서비스 URL: {}, 파일 이름: {}", serviceUrl, file.getOriginalFilename(), e);
            throw new RuntimeException("임시 파일 저장 실패", e);
        }
    }

    @Transactional
    public void saveMeongPhotoToS3FromTempFile() {
        String serviceUrl = "/api/v1/photos";
        int batchSize = 10;
        Long lastProcessedId = 0L;

        while (true) {
            // 1. 배치 크기만큼 데이터 조회하기
            Pageable pageable = PageRequest.of(0, batchSize);
            List<TempFile> tempFiles = tempFileRepository.findAllByServiceUrlAndLastId(serviceUrl, lastProcessedId, pageable);

            // 데이터가 없으면 종료
            if (tempFiles.isEmpty()) {
                break;
            }

            // 2. S3 업로드 및 MediaFile / MeongPhoto 테이블 저장
            for (TempFile tempFile : tempFiles) {
                try {
                    // 2-1. S3 업로드
                    InputStream inputStream = new ByteArrayInputStream(tempFile.getFileData());
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS"));
                    String suffix = "_" + timestamp + "_meongPhoto.jpg";

                    ObjectMetadata metadata = new ObjectMetadata();
                    metadata.setContentLength(tempFile.getFileData().length);
                    metadata.setContentType(tempFile.getContentType());

                    String s3Key = "MeongPhoto/" + tempFile.getOwnerId() + suffix;
                    s3Client.putObject(new PutObjectRequest(bucket, s3Key, inputStream, metadata));

                    // 2-2. MediaFile 엔티티 저장
                    ImageMetadataDto imageMetadata = mediaFileService.extractImageMetadata(tempFile.getFileData());
                    MediaFile mediaFile = mediaFileService.saveMediaFile(imageMetadata,
                            new S3UploadResultDto("https://your-bucket-name.s3.amazonaws.com/" + s3Key, s3Key)
                    );

                    // 2-3. MeongPhoto 엔티티 저장
                    Member member = memberRepository.findById(tempFile.getOwnerId())
                            .orElseThrow(() -> NotFoundException.entityNotFound("멤버"));
                    MeongPhoto meongPhoto = MeongPhoto.createMeongPhoto(member, mediaFile);
                    meongPhotoRepository.save(meongPhoto);

                    // 2-4. tempFile 삭제
                    tempFileRepository.delete(tempFile);

                } catch (Exception e) {
                    log.error("임시 파일 처리 중 오류 발생. TempFile ID: {}, TempFile: {} ", tempFile.getOwnerId(), tempFile.getFileName(), e);
                }

                lastProcessedId = tempFile.getTempFileId();
            }
        }

        log.info("ServiceUrl '{}'에 대한 임시 파일 복구 완료", serviceUrl);
    }

}

