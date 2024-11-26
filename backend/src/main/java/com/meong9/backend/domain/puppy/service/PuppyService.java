package com.meong9.backend.domain.puppy.service;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.puppy.dto.PuppyProfileResponseDto;
import com.meong9.backend.domain.puppy.dto.PuppyRequestDto;
import com.meong9.backend.domain.puppy.dto.PuppyResponseDto;
import com.meong9.backend.domain.puppy.entity.Breed;
import com.meong9.backend.domain.puppy.entity.Puppy;
import com.meong9.backend.domain.puppy.repository.PuppyRepository;
import com.meong9.backend.global.exception.NotFoundException;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PuppyService {

    private final PuppyRepository puppyRepository;
    private final MediaFileRepository mediaFileRepository;
    private final MediaFileService mediaFileService;
    private final BreedService breedService;

    /**
     * 강아지 프로필 생성
     */
    @Transactional
    public PuppyResponseDto createPuppy(PuppyRequestDto puppyRequestDto, MultipartFile image, Member currentMember) throws IOException {
        MediaFile profileImage = null; // 업로드된 파일을 추적하기 위한 변수
        try {
            // 1. 이미지 업로드 처리
            profileImage = handleImageUpload(image, puppyRequestDto.getName());

            // 2. 품종 조회
            Breed breed = findBreed(puppyRequestDto.getBreedId());

            // 3. Puppy 엔티티 생성
            Puppy puppy = createPuppyEntity(puppyRequestDto, currentMember, breed, profileImage);

            // 4. 저장 후 응답 반환
            Puppy savedPuppy = puppyRepository.save(puppy);
            return new PuppyResponseDto(savedPuppy.getPuppyId());

        } catch (Exception e) {
            // 트랜잭션 실패 시 업로드된 파일 삭제
            if (profileImage != null && profileImage.getFileKey() != null) {
                mediaFileService.deleteFromS3(profileImage.getFileKey());
            }
            throw e; // 예외 다시 던짐
        }
    }

    /**
     * 강아지 프로필 조회
     */
    @Transactional(readOnly = true)
    public PuppyProfileResponseDto getPuppyProfile(Long puppyId) {
        // PuppyRepository에서 강아지 프로필 조회
        return puppyRepository.findPuppyProfileById(puppyId)
                .orElseThrow(() -> new IllegalArgumentException("강아지를 찾을 수 없습니다."));
    }

    /**
     * 강아지 프로필 수정
     */
    @Transactional
    public PuppyResponseDto updatePuppy(Long puppyId, PuppyRequestDto updateRequest, MultipartFile image) throws IOException {
        MediaFile profileImage = null; // 업로드된 새 파일을 추적하기 위한 변수
        try {
            // 1. 강아지 조회
            Puppy puppy = findPuppyById(puppyId);

            // 2. 품종 조회
            Breed breed = findBreed(updateRequest.getBreedId());

            // 3. 이미지 업데이트 처리
            profileImage = handleImageUpdate(image, updateRequest.getName(), puppy.getProfileImageId());

            // 4. 엔티티 업데이트
            puppy.update(updateRequest.getName(), updateRequest.getBirthDate(), updateRequest.getGender(),
                    updateRequest.getWeight(), updateRequest.getNeutered(), breed, profileImage);

            // 5. 수정된 강아지 ID 반환
            return new PuppyResponseDto(puppy.getPuppyId());

        } catch (Exception e) {
            // 트랜잭션 실패 시 업로드된 새 파일 삭제
            if (profileImage != null && profileImage.getFileKey() != null) {
                mediaFileService.deleteFromS3(profileImage.getFileKey());
            }
            throw e; // 예외 다시 던짐
        }
    }

    /**
     * 강아지 프로필 삭제
     */
    @Transactional
    public void deletePuppyById(Long puppyId) {
        // 1. 강아지 조회
        Puppy puppy = findPuppyById(puppyId);

        // 2. 프로필 이미지 삭제
        deleteImage(puppy.getProfileImageId());

        // 3. 강아지 엔티티 삭제
        puppyRepository.delete(puppy);
    }

    /**
     * 품종 조회
     * @param breedId 품종 ID
     * @return 조회된 Breed 엔티티
     */
    private Breed findBreed(Long breedId) {
        return breedService.findBreedById(breedId);
    }

    /**
     * 강아지 조회
     * @param puppyId 강아지 ID
     * @return 조회된 Puppy 엔티티
     */
    private Puppy findPuppyById(Long puppyId) {
        return puppyRepository.findById(puppyId).orElseThrow(()-> NotFoundException.entityNotFound("Puppy"));
    }

    /**
     * 이미지 업로드 처리
     * @param image 업로드할 이미지
     * @param name 강아지 이름
     * @return 저장된 MediaFile 엔티티
     * @throws IOException 이미지 처리 오류
     */
    private MediaFile handleImageUpload(MultipartFile image, String name) throws IOException {
        if (image == null || image.isEmpty()) {
            return null; // 이미지가 없으면 null 반환
        }
        String fileKey = generateFileKey(name); // S3 파일 키 생성
        return saveImage(image, fileKey); // 이미지 저장
    }

    /**
     * 이미지 업데이트 처리
     * @param image 업로드할 새 이미지
     * @param name 강아지 이름
     * @param existingImage 기존 MediaFile 엔티티
     * @return 저장된 MediaFile 엔티티
     * @throws IOException 이미지 처리 오류
     */
    private MediaFile handleImageUpdate(MultipartFile image, String name, MediaFile existingImage) throws IOException {
        if (image != null && !image.isEmpty()) {
            // 기존 이미지 삭제
            deleteImage(existingImage);
            String fileKey = generateFileKey(name); // 새 파일 키 생성
            return saveImage(image, fileKey); // 새 이미지 저장
        }
        return existingImage; // 새 이미지가 없으면 기존 이미지 반환
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
     * @param name 강아지 이름
     * @return 생성된 파일 키
     */
    private String generateFileKey(String name) {
        // 현재 시간 기반으로 파일 키 생성
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return "Pprofile/" + name + "_" + LocalDateTime.now().format(formatter) + "_profile.jpg";
    }

    /**
     * Puppy 엔티티 생성
     * @param dto 강아지 요청 데이터
     * @param member 강아지를 소유한 회원
     * @param breed 품종 정보
     * @param profileImage 프로필 이미지
     * @return 생성된 Puppy 엔티티
     */
    private Puppy createPuppyEntity(PuppyRequestDto dto, Member member, Breed breed, MediaFile profileImage) {
        // Puppy 엔티티 생성 및 반환
        return new Puppy(null, dto.getName(), dto.getBirthDate(), dto.getGender(), dto.getWeight(),
                dto.getNeutered(), member, breed, profileImage);
    }
}
