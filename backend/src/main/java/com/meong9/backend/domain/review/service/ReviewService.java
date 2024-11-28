package com.meong9.backend.domain.review.service;


import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.review.dto.ReviewRequestDto;
import com.meong9.backend.domain.review.repository.ReviewFileRepository;
import com.meong9.backend.domain.review.repository.ReviewRepository;
import com.meong9.backend.global.mediafile.dto.ImageMetadataDto;
import com.meong9.backend.global.mediafile.entity.FileType;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import com.meong9.backend.global.mediafile.repository.MediaFileRepository;
import com.meong9.backend.global.mediafile.service.MediaFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final MediaFileService mediaFileService;
    private final ReviewRepository reviewRepository;
    private final ReviewFileRepository reviewFileRepository;
    private final MediaFileRepository mediaFileRepository;

    @Transactional
    public void createPuppy(ReviewRequestDto reviewRequestDto, List<MultipartFile> files, Member member) throws IOException {
        MediaFile images = null;
        for (MultipartFile file : files) { // 파일 저장
        }
        try {
            // 1. 품종 조회
            Breed breed = findBreed(puppyRequestDto.getBreedId());

            // 2. Puppy 엔티티 생성 및 초기 저장
            Puppy puppy = createPuppyEntity(puppyRequestDto, currentMember, breed);
            Puppy savedPuppy = puppyRepository.save(puppy); // puppyId 생성됨

            // 3. 이미지 업로드 처리
            profileImage = handleImageUpload(image, savedPuppy.getPuppyId());
            savedPuppy.setProfileImage(profileImage);

            // 4. Puppy 엔티티 업데이트
            puppyRepository.save(savedPuppy);



        } catch (Exception e) {
            // 트랜잭션 실패 시 업로드된 파일 삭제
            if (profileImage != null && profileImage.getFileKey() != null) {
                mediaFileService.deleteFromS3(profileImage.getFileKey());
            }
            throw e; // 예외 다시 던짐
        }
    }


    // todo: 아래 메서드는 따로 static class 만들어야할듯
    /**
     * 이미지 업로드 처리
     * @param image 업로드할 이미지
     * @param id 강아지 id
     * @return 저장된 MediaFile 엔티티
     * @throws IOException 이미지 처리 오류
     */
    private MediaFile handleImageUpload(MultipartFile image, Long id) throws IOException {
        if (image == null || image.isEmpty()) {
            return null; // 이미지가 없으면 null 반환
        }
        String fileKey = generateFileKey(id); // S3 파일 키 생성
        return saveImage(image, fileKey); // 이미지 저장
    }

    /**
     * 이미지 업데이트 처리
     * @param image 업로드할 새 이미지
     * @param puppyId 강아지 ID
     * @param existingImage 기존 MediaFile 엔티티
     * @return 저장된 MediaFile 엔티티
     * @throws IOException 이미지 처리 오류
     */
    private MediaFile handleImageUpdate(MultipartFile image, Long puppyId, MediaFile existingImage) throws IOException {
        deleteImage(existingImage);
        String fileKey = generateFileKey(puppyId); // 새 파일 키 생성
        return saveImage(image, fileKey); // 새 이미지 저장
    }

    /**
     * 이미지 삭제
     * @param profileImage 삭제할 MediaFile 엔티티
     */
    private void deleteImage(MediaFile profileImage) {
        if (profileImage != null) {
            // S3에서 파일 삭제
            if (profileImage.getFileKey() != null) {
                mediaFileService.deleteFromS3(profileImage.getFileKey());
            }
            // 데이터베이스에서 MediaFile 삭제
            mediaFileRepository.delete(profileImage);
        }
    }

    /**
     * 이미지 저장
     * @param image 저장할 이미지 파일
     * @param fileKey S3에 저장할 파일 키
     * @return 저장된 MediaFile 엔티티
     * @throws IOException 이미지 처리 오류
     */
    private MediaFile saveImage(MultipartFile image, String fileKey) throws IOException {
        // S3에 파일 업로드
        String imageUrl = mediaFileService.uploadToS3WithCustomKey(image, fileKey);
        // 업로드한 파일의 메타데이터 추출
        ImageMetadataDto metadata = mediaFileService.extractImageMetadata(image);

        // MediaFile 엔티티 저장
        return mediaFileRepository.save(
                MediaFile.builder()
                        .fileType(FileType.IMAGE)
                        .fileSize((int) image.getSize())
                        .fileName(image.getOriginalFilename())
                        .fileUrl(imageUrl)
                        .height((double) metadata.getHeight())
                        .width((double) metadata.getWidth())
                        .fileKey(fileKey)
                        .build());
    }

    /**
     * S3 파일 키 생성
     * @param domain 경로 지정
     * @param id 강아지 ID
     * @return 생성된 파일 키
     */
    private String generateFileKey(String domain,Long id) {
        return "Profile/" + puppyId + "_profile.jpg";
    }
}
