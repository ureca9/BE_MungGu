package com.meong9.backend.domain.meongPhoto.service;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.meongPhoto.dto.MeongPhotoListDto;
import com.meong9.backend.domain.meongPhoto.dto.MeongPhotoResponseDto;
import com.meong9.backend.domain.meongPhoto.entity.MeongPhoto;
import com.meong9.backend.domain.meongPhoto.entity.id.MeongPhotoId;
import com.meong9.backend.domain.meongPhoto.repository.MeongPhotoRepository;
import com.meong9.backend.global.exception.InternalServerError;
import com.meong9.backend.global.mediafile.dto.ImageMetadataDto;
import com.meong9.backend.global.mediafile.dto.S3UploadResultDto;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import com.meong9.backend.global.mediafile.service.MediaFileService;
import com.meong9.backend.global.tempFile.service.TempFileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "MeongPhotoService")
public class MeongPhotoService {

    private final MediaFileService mediaFileService;
    private final MeongPhotoRepository meongPhotoRepository;
    private final TempFileService tempFileService;

    /**
     * 멍생네컷을 S3에 저장한 후 다운로드 url을 제공하는 서비스 메서드
     */
    @Transactional
    public MeongPhotoResponseDto createMeongPhoto(Member member, MultipartFile file, String serviceUrl) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String suffix = "_" + timestamp + "_meongPhoto.jpg";
            S3UploadResultDto s3UploadResultDto = mediaFileService.uploadMeongPhoto(
                    file, member.getMemberId(),"MeongPhoto/", suffix);

            ImageMetadataDto metadata = mediaFileService.extractImageMetadata(file);
            MediaFile savedMeongPhoto = mediaFileService.saveMediaFile(metadata, s3UploadResultDto);

            String downloadImageUrl = mediaFileService.generateDownloadUrl(s3UploadResultDto.getS3Url(), 1);

            MeongPhotoId meongPhotoId = new MeongPhotoId(member.getMemberId(), savedMeongPhoto.getMediaFileId());
            MeongPhoto meongPhoto = new MeongPhoto(meongPhotoId, member, savedMeongPhoto);
            meongPhotoRepository.save(meongPhoto);

            return new MeongPhotoResponseDto(s3UploadResultDto.getS3Url(), downloadImageUrl);
        } catch (IOException e) {
            log.error("멍생네컷 저장 중 오류 발생: {}", e.getMessage(), e);
            tempFileService.saveTemporaryFile(serviceUrl, file);
            throw InternalServerError.photoProcessingError();
        }
    }

    public MeongPhotoListDto getAllMeongPhoto(Long lastPhotoId) {
        return null;
    }
}
