package com.meong9.backend.domain.review.service;


import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.review.dto.ReviewRequestDto;
import com.meong9.backend.domain.review.entity.Review;
import com.meong9.backend.domain.review.entity.ReviewFile;
import com.meong9.backend.domain.review.repository.ReviewFileRepository;
import com.meong9.backend.domain.review.repository.ReviewRepository;
import com.meong9.backend.global.exception.NotFoundException;
import com.meong9.backend.global.mediafile.dto.ImageMetadataDto;
import com.meong9.backend.global.mediafile.entity.FileType;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import com.meong9.backend.global.mediafile.repository.MediaFileRepository;
import com.meong9.backend.global.mediafile.service.MediaFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final MediaFileService mediaFileService;
    private final ReviewRepository reviewRepository;
    private final ReviewFileRepository reviewFileRepository;
    private final MediaFileRepository mediaFileRepository;

    @Transactional
    public void createReview(ReviewRequestDto reviewRequestDto, List<MultipartFile> files, Member member) throws IOException {

        List<ReviewFile> reviewFiles=new ArrayList<>();
        Review review=Review.builder()
                .member(member)
                .content(reviewRequestDto.getContent())
                .nickname(member.getNickname())
                .score(reviewRequestDto.getScore())
                .type(reviewRequestDto.getType())
                .placePensionId(reviewRequestDto.getPlcPenId())
                .reviewFiles(new ArrayList<>())
                .build();

        Review savedReview = reviewRepository.save(review);

        List<MediaFile> mediaFiles=new ArrayList<>();
        // ReviewFile들을 생성
        if(files != null) {
            for (MultipartFile mf : files) { // 파일들 저장
                MediaFile file = handleImageUpload(mf, savedReview.getReviewId());
                mediaFiles.add(file);
                ReviewFile reviewFile=new ReviewFile(file);
                reviewFileRepository.save(reviewFile);
                reviewFiles.add(reviewFile);
            }
        }
        review.setReviewFiles(reviewFiles);

        // 트랜잭션 동기화
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                            mediaFiles.forEach(file -> mediaFileService.deleteFromS3(file.getFileKey()));
                        }
                    }
                }
        );
    }


    @Transactional
    public void updateReview(Long reviewId, ReviewRequestDto reviewRequestDto, List<MultipartFile> files, Member member) throws IOException, IllegalAccessException {
        // 1. 기존 리뷰 조회 및 권한 검증
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("리뷰"));
        if (!review.getMember().equals(member)) {
            throw new IllegalAccessException("리뷰 작성자만 수정이 가능합니다"); // 임시로 씀
        }

        // 2. 기존 파일 관리
        List<ReviewFile> existingReviewFiles = review.getReviewFiles();
        List<ReviewFile> newReviewFiles = new ArrayList<>();
        List<MediaFile> mediaFiles=new ArrayList<>();

        if (files != null) {
            // 새로운 파일 저장
            for (MultipartFile mf : files) {
                MediaFile file = handleImageUpload(mf, review.getReviewId());
                mediaFiles.add(file);
                ReviewFile reviewFile=new ReviewFile(file);
                reviewFileRepository.save(reviewFile);
                newReviewFiles.add(reviewFile);
            }
        } else {
            // 기존의 파일 삭제
            for (ReviewFile reviewFile : existingReviewFiles) {
                deleteReviewFile(reviewFile);
            }
        }

        // 리뷰 정보 업데이트
        review.update(reviewRequestDto);
    }

    private void deleteReviewFile(ReviewFile reviewFile) {
        mediaFileService.deleteFromS3(reviewFile.getFile().getFileKey());
        mediaFileRepository.delete(reviewFile.getFile());
        reviewFileRepository.delete(reviewFile);
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
     * @param reviewId 리뷰 ID
     * @param existingImage 기존 MediaFile 엔티티
     * @return 저장된 MediaFile 엔티티
     * @throws IOException 이미지 처리 오류
     */
    private MediaFile handleImageUpdate(MultipartFile image, Long reviewId, MediaFile existingImage) throws IOException {
        deleteImage(existingImage);
        String fileKey = generateFileKey(reviewId); // 새 파일 키 생성
        return saveImage(image, fileKey); // 새 이미지 저장
    }

    /**
     * 이미지 삭제
     * @param reviewImage 삭제할 MediaFile 엔티티
     */
    private void deleteImage(MediaFile reviewImage) {
        if (reviewImage != null) {
            // S3에서 파일 삭제
            if (reviewImage.getFileKey() != null) {
                mediaFileService.deleteFromS3(reviewImage.getFileKey());
            }
            // 데이터베이스에서 MediaFile 삭제
            mediaFileRepository.delete(reviewImage);
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
     * @param reviewId 강아지 ID
     * @return 생성된 파일 키
     */
    private String generateFileKey(Long reviewId) {
        return "Review/" + reviewId + "_review.jpg";
    }

}
