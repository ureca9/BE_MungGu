package com.meong9.backend.domain.review.service;


import com.meong9.backend.domain.address.entity.PlcPenAddress;
import com.meong9.backend.domain.address.repository.PlcPenAddressRepository;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.pension.entity.Pension;
import com.meong9.backend.domain.pension.repository.PensionRepository;
import com.meong9.backend.domain.place.entity.Place;
import com.meong9.backend.domain.place.repository.PlaceRepository;
import com.meong9.backend.domain.review.dto.ReviewMainDto;
import com.meong9.backend.domain.review.dto.MyReviewResponseDto;
import com.meong9.backend.domain.review.dto.ReviewDetailsResponseDto;
import com.meong9.backend.domain.review.dto.ReviewRequestDto;
import com.meong9.backend.domain.review.entity.Review;
import com.meong9.backend.domain.review.entity.ReviewFile;
import com.meong9.backend.domain.review.entity.id.ReviewFileId;
import com.meong9.backend.domain.review.repository.ReviewFileRepository;
import com.meong9.backend.domain.review.repository.ReviewRepository;
import com.meong9.backend.global.exception.NotFoundException;
import com.meong9.backend.global.mediafile.dto.ImageMetadataDto;
import com.meong9.backend.global.mediafile.entity.FileType;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import com.meong9.backend.global.mediafile.repository.MediaFileRepository;
import com.meong9.backend.global.mediafile.service.MediaFileService;
import com.meong9.backend.global.utils.AddressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {
    private final MediaFileService mediaFileService;
    private final ReviewRepository reviewRepository;
    private final ReviewFileRepository reviewFileRepository;
    private final MediaFileRepository mediaFileRepository;
    private final PensionRepository pensionRepository;
    private final PlcPenAddressRepository plcPenAddressRepository;
    private final PlaceRepository placeRepository;


    @Transactional(readOnly = true)
    public ReviewDetailsResponseDto getReviewDetails(Long reviewId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()->NotFoundException.entityNotFound("리뷰"));

        return ReviewDetailsResponseDto.from(review);
    }

    @Transactional(readOnly = true)
    public List<MyReviewResponseDto> getMyReviews(Member member) {
        List<Review> reviews = reviewRepository.findByMember(member);
        return reviews.stream().map(MyReviewResponseDto::from).toList();
    }

    @Transactional
    public void createReview(ReviewRequestDto reviewRequestDto, List<MultipartFile> files, Member member) throws IOException {
        List<MediaFile> mediaFiles = new ArrayList<>();
        Review review = Review.builder()
                .member(member)
                .content(reviewRequestDto.getContent())
                .nickname(member.getNickname())
                .score(reviewRequestDto.getScore())
                .type(reviewRequestDto.getType())
                .placePensionId(reviewRequestDto.getPlcPenId())
                .reviewFiles(null)
                .build();

        Review savedReview = reviewRepository.save(review);

        if (files != null) {
            List<ReviewFile> reviewFiles = new ArrayList<>();
            for (MultipartFile mf : files) {
                MediaFile file = handleImageUpload(mf, savedReview.getReviewId());
                mediaFiles.add(file);

                // 복합 키 생성
                ReviewFileId reviewFileId = new ReviewFileId(savedReview.getReviewId(), file.getMediaFileId());

                // 객체가 이미 존재하면 가져오고, 없으면 새로 생성
                ReviewFile reviewFile = reviewFileRepository.findById(reviewFileId)
                        .orElseGet(() ->
                                ReviewFile.builder()
                                        .review(savedReview)
                                        .file(file)
                                        .reviewFileId(reviewFileId)
                                        .build()
                        );

                reviewFileRepository.save(reviewFile);
                reviewFiles.add(reviewFile);
            }
        }

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
        // 기존 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()->NotFoundException.entityNotFound("리뷰"));

        // 작성자 권한 확인
        if (!review.getMember().getMemberId().equals(member.getMemberId())) {
            // todo: 커스텀 예외 따로 만들어야함
            throw new IllegalAccessException("리뷰 수정 권한이 없습니다.");
        }

        // 기존 파일 삭제
        List<ReviewFile> existingFiles = reviewFileRepository.findByReview(review);
        for (ReviewFile reviewFile : existingFiles) {
            reviewFileRepository.delete(reviewFile);
            mediaFileService.deleteFromS3(reviewFile.getFile().getFileKey()); // S3에서 파일 삭제
        }

        review.update(reviewRequestDto);

        // 5. 새로운 파일 처리
        List<MediaFile> mediaFiles = new ArrayList<>();
        List<ReviewFile> newReviewFiles = new ArrayList<>();
        if (files != null) {
            for (MultipartFile mf : files) {
                MediaFile file = handleImageUpload(mf, review.getReviewId());
                mediaFiles.add(file);

                // 복합 키 생성
                ReviewFileId reviewFileId = new ReviewFileId(review.getReviewId(), file.getMediaFileId());

                // 새 ReviewFile 생성
                ReviewFile reviewFile = ReviewFile.builder()
                        .review(review)
                        .file(file)
                        .reviewFileId(reviewFileId)
                        .build();

                reviewFileRepository.save(reviewFile);
                newReviewFiles.add(reviewFile);
            }
        }

        // 리뷰에 새로운 파일 연결
        review.setReviewFiles(newReviewFiles);

        //트랜잭션 동기화
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
    public void deleteReview(Long reviewId, Member member) throws IllegalAccessException {
        // 리뷰 조회 및 권한 확인
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("리뷰"));

        if (!review.getMember().equals(member)) {
            throw new IllegalAccessException("리뷰 작성자만 삭제가 가능합니다");
        }

        // 연관된 파일 삭제
        if (review.getReviewFiles() != null) {
            for (ReviewFile reviewFile : review.getReviewFiles()) {
                mediaFileService.deleteFromS3(reviewFile.getFile().getFileKey());
            }
        }

        // 리뷰 삭제 (ReviewFile은 CascadeType.ALL로 자동 삭제)
        reviewRepository.delete(review);
    }

    // 최근 리뷰 10개 조회
    @Transactional(readOnly = true)
    public List<ReviewMainDto> getRecentReviews() {
        // 리뷰 조회
        List<Review> reviewList = reviewRepository.findTop10RecentReviews();

        // plc_pen_id와 type 수집
        List<Long> pensionIds = new ArrayList<>();
        List<Long> placeIds = new ArrayList<>();
        for (Review review : reviewList) {
            if ("020".equals(review.getType()) && review.getPlacePensionId() != null) {
                pensionIds.add(review.getPlacePensionId());
            } else if ("010".equals(review.getType()) && review.getPlacePensionId() != null) {
                placeIds.add(review.getPlacePensionId());
            }
        }

        // 펜션, 시설 데이터 한번에 조회
        Map<Long, Pension> pensionMap = getPensionMap(pensionIds);
        Map<Long, Place> placeMap = getPlaceMap(placeIds);

        // 주소 데이터 한번에 조회
        Map<Long, PlcPenAddress> addressMap = getAddressMap(pensionIds, placeIds);

        // 리뷰
        List<ReviewMainDto> reviewMainDtos = new ArrayList<>();
        for (Review review : reviewList) {
            if ("020".equals(review.getType())) {
                Pension pension = pensionMap.get(review.getPlacePensionId());
                PlcPenAddress plcPenAddress = addressMap.get(review.getPlacePensionId());
                reviewMainDtos.add(createPensionReviewDto(review, pension, plcPenAddress));
            } else if ("010".equals(review.getType())) {
                Place place = placeMap.get(review.getPlacePensionId());
                PlcPenAddress plcPenAddress = addressMap.get(review.getPlacePensionId());
                reviewMainDtos.add(createPlaceReviewDto(review, place, plcPenAddress));
            }
        }

        return reviewMainDtos;
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


    // 펜션 데이터 한번에
    private Map<Long, Pension> getPensionMap(List<Long> pensionIds) {
        List<Pension> pensionList = pensionRepository.findAllDataByIds(pensionIds);
        Map<Long, Pension> pensionMap = new HashMap<>();
        for (Pension pension : pensionList) {
            pensionMap.put(pension.getPensionId(), pension);
        }
        return pensionMap;
    }

    // 시설 데이터 한번에
    private Map<Long, Place> getPlaceMap(List<Long> placeIds) {
        if (placeIds == null || placeIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Place> placeList = placeRepository.findAllDataByIds(placeIds);
        Map<Long, Place> placeMap = new HashMap<>();
        for (Place place : placeList) {
            placeMap.put(place.getPlaceId(), place);
        }
        return placeMap;
    }

    // 주소 데이터 한번에
    private Map<Long, PlcPenAddress> getAddressMap(List<Long> pensionIds, List<Long> placeIds) {
        if ((pensionIds == null || pensionIds.isEmpty()) && (placeIds == null || placeIds.isEmpty())) {
            return Collections.emptyMap();
        }

        List<PlcPenAddress> addressList = plcPenAddressRepository.findByPlcPenIdInAndType(pensionIds, placeIds);

        Map<Long, PlcPenAddress> addressMap = new HashMap<>();
        for (PlcPenAddress address : addressList) {
            addressMap.put(address.getPlcPenId(), address);
        }

        return addressMap;
    }

    // 펜션 리뷰 dto
    private ReviewMainDto createPensionReviewDto(Review review, Pension pension, PlcPenAddress plcPenAddress) {
        String addressInfo = AddressMapper.formatAddress(plcPenAddress);

        String img = getReviewImageUrl(review);

        String reviewAvg = pension != null ? formatReviewAvg(pension.getReviewAvg()) : "0.0";
        int reviewCount = pension != null ? pension.getReviewCount() : 0;

        return ReviewMainDto.builder()
                .id(review.getPlacePensionId())
                .name(pension != null ? pension.getName() : null)
                .address(addressInfo)
                .img(img)
                .reviewAvg(reviewAvg)
                .reviewCount(reviewCount)
                .reviewContent(review.getContent())
                .nickname(review.getNickname())
                .reviewId(review.getReviewId())
                .type("펜션")
                .build();
    }

    private ReviewMainDto createPlaceReviewDto(Review review, Place place, PlcPenAddress plcPenAddress) {
        String addressInfo = AddressMapper.formatAddress(plcPenAddress);

        String img = getReviewImageUrl(review);

        String reviewAvg = place != null ? formatReviewAvg(place.getReviewAvg()) : "0.0";
        int reviewCount = place != null ? place.getReviewCount() : 0;

        return ReviewMainDto.builder()
                .id(review.getPlacePensionId())
                .name(place != null ? place.getName() : null)
                .address(addressInfo)
                .img(img) // 장소 이미지 처리 필요
                .reviewAvg(reviewAvg)
                .reviewCount(reviewCount)
                .reviewContent(review.getContent())
                .nickname(review.getNickname())
                .reviewId(review.getReviewId())
                .type("시설")
                .build();
    }


    // 리뷰 평균 0.0으로
    private String formatReviewAvg(Double reviewAvg) {
        return reviewAvg != null ? new DecimalFormat("#.#").format(reviewAvg) : "0.0";
    }

    // 리뷰 이미지
    private String getReviewImageUrl(Review review){
        return !review.getReviewFiles().isEmpty() ? review.getReviewFiles().get(0).getFile().getFileUrl() : null;
    }

}
