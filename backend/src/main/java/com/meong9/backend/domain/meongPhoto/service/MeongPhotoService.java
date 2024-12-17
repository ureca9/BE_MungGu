package com.meong9.backend.domain.meongPhoto.service;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.meongPhoto.dto.*;
import com.meong9.backend.domain.meongPhoto.entity.MeongPhoto;
import com.meong9.backend.domain.meongPhoto.repository.MeongPhotoRepository;
import com.meong9.backend.global.mediafile.dto.ImageMetadataDto;
import com.meong9.backend.global.mediafile.dto.S3UploadResultDto;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import com.meong9.backend.global.mediafile.service.MediaFileService;
import com.meong9.backend.global.tempFile.service.TempFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

            MeongPhoto meongPhoto = MeongPhoto.createMeongPhoto(member, savedMeongPhoto);
            meongPhotoRepository.save(meongPhoto);

            return new MeongPhotoResponseDto(s3UploadResultDto.getS3Url(), downloadImageUrl, false);
        } catch (IOException e) {
            log.error("멍생네컷 저장 중 오류 발생: {}", e.getMessage(), e);
            tempFileService.saveTemporaryFile(serviceUrl, file, member.getMemberId(), "MEMBER");
            return new MeongPhotoResponseDto(null, null, true);
        }
    }

    @Transactional(readOnly = true)
    public MeongPhotoListDto getAllMeongPhoto(Pageable pageable) {
        Slice<MeongPhotoDto> meongPhotos = meongPhotoRepository.findAllWithPagination(pageable);
        return new MeongPhotoListDto(meongPhotos.getContent(), meongPhotos.hasNext());
    }

    @Transactional(readOnly = true)
    public MyMeongPhotoListDto getMyMeongPhotos(Member member, Pageable pageable) {
        Slice<MyMeongPhotoDto> myMeongPhotos = meongPhotoRepository.findAllByMemberIdWithPagination(
                member.getMemberId(), pageable);
        return new MyMeongPhotoListDto(myMeongPhotos.getContent(), myMeongPhotos.hasNext());
    }

}
