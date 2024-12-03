package com.meong9.backend.domain.meongPhoto.service;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.meongPhoto.dto.MeongPhotoResponseDto;
import com.meong9.backend.global.mediafile.dto.ImageMetadataDto;
import com.meong9.backend.global.mediafile.dto.S3UploadResultDto;
import com.meong9.backend.global.mediafile.service.MediaFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class MeongPhotoService {

    private final MediaFileService mediaFileService;

    public MeongPhotoResponseDto createMeongPhoto(Member member, MultipartFile file) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String suffix = "_" + timestamp + "_meongPhoto.jpg";
        S3UploadResultDto s3UploadResultDto = mediaFileService.uploadMeongPhoto(
                file, member.getMemberId(),"MeongPhoto/", suffix);

        ImageMetadataDto metadata = mediaFileService.extractImageMetadata(file);
        mediaFileService.saveMediaFile(metadata, s3UploadResultDto);
        String downloadImageUrl = mediaFileService.generateDownloadUrl(s3UploadResultDto.getS3Url(), 1);

        return new MeongPhotoResponseDto(s3UploadResultDto.getS3Url(), downloadImageUrl);
    }


}
