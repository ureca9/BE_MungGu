package com.meong9.backend.domain.review.service;


import com.meong9.backend.domain.address.entity.PlcPenAddress;
import com.meong9.backend.domain.address.repository.PlcPenAddressRepository;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.member.repository.MemberRepository;
import com.meong9.backend.domain.pension.entity.Pension;
import com.meong9.backend.domain.pension.repository.PensionRepository;
import com.meong9.backend.domain.place.entity.Place;
import com.meong9.backend.domain.place.repository.PlaceRepository;
import com.meong9.backend.domain.review.dto.*;
import com.meong9.backend.domain.review.entity.Review;
import com.meong9.backend.domain.review.entity.ReviewFile;
import com.meong9.backend.domain.review.entity.id.ReviewFileId;
import com.meong9.backend.domain.review.repository.ReviewFileRepository;
import com.meong9.backend.domain.review.repository.ReviewRepository;
import com.meong9.backend.global.exception.NotFoundException;
import com.meong9.backend.global.mediafile.dto.ImageMetadataDto;
import com.meong9.backend.global.mediafile.dto.VideoMetaDataDto;
import com.meong9.backend.global.mediafile.entity.FileType;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import com.meong9.backend.global.mediafile.repository.MediaFileRepository;
import com.meong9.backend.global.mediafile.service.MediaFileService;
import com.meong9.backend.global.utils.AddressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
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
    private final MemberRepository memberRepository;


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
    public void createReview(ReviewRequestDto reviewRequestDto, List<MultipartFile> files, Member member) throws IOException, InterruptedException {
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
            AtomicInteger fileNum = new AtomicInteger(0);
            for (MultipartFile mf : files) {
                MediaFile file = handleFileUpload(mf, savedReview.getReviewId(),fileNum);
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

                reviewFiles.add(reviewFile);
                reviewFileRepository.save(reviewFile);
                fileNum.getAndIncrement();
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

    /**
     * 이미지 업로드 처리
     * @param file 업로드할 파일
     * @param reviewId 리뷰 id
     * @return 저장된 MediaFile 엔티티
     * @throws IOException 이미지 처리 오류
     */
    private MediaFile handleFileUpload(MultipartFile file, Long reviewId, AtomicInteger fileNum) throws IOException, InterruptedException {
        String contentType = file.getContentType();
        String fileKey = generateFileKey(reviewId,fileNum); // 새 파일 키 생성
        if (contentType == null) {
            throw new IllegalArgumentException("파일 형식이 정의되지 않았습니다.");
        }

        if (contentType.startsWith("image/")) {
            return saveImage(file, fileKey); // 이미지 저장
        } else if (contentType.startsWith("video/")) {
            // 동영상 처리
            return saveVideo(file, fileKey);
        } else {
            throw new IllegalArgumentException("지원되지 않는 content type: " + contentType);
        }
    }




    @Transactional
    public void updateReview(Long reviewId, ReviewRequestDto reviewRequestDto, List<MultipartFile> files, Member member) throws IOException, IllegalAccessException, InterruptedException {
        // 기존 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> NotFoundException.entityNotFound("리뷰"));

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
        AtomicInteger fileNum = new AtomicInteger(0);
        if (files != null) {
            for (MultipartFile mf : files) {
                MediaFile file = handleFileUpload(mf, review.getReviewId(),fileNum);
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

    /**
     * 이미지 업데이트 처리
     * @param file 업로드할 새 파일
     * @param reviewId 리뷰 ID
     * @param existingFile 기존 MediaFile 엔티티
     * @return 저장된 MediaFile 엔티티
     * @throws IOException 이미지 처리 오류
     */
    private MediaFile handleFileUpdate(MultipartFile file, Long reviewId, MediaFile existingFile,AtomicInteger fileNum) throws IOException {
        deleteImage(existingFile);
        String fileKey = generateFileKey(reviewId,fileNum); // 새 파일 키 생성
        return saveImage(file, fileKey); // 새 이미지 저장
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

    private MediaFile saveVideo(MultipartFile video, String fileKey) throws IOException, InterruptedException {
        // S3에 파일 업로드
        String videoUrl = mediaFileService.uploadToS3WithCustomKey(video, fileKey);

        // 업로드한 파일의 메타데이터 추출
        VideoMetaDataDto metadata = mediaFileService.extractVideoMetadata(video);

        // MediaFile 엔티티 저장
        return mediaFileRepository.save(
                MediaFile.builder()
                        .fileType(FileType.VIDEO)
                        .fileSize((int) video.getSize())
                        .fileName(video.getOriginalFilename())
                        .fileUrl(videoUrl)
                        .height((double) metadata.getHeight())
                        .width((double) metadata.getWidth())
                        .fileKey(fileKey)
                        .build());
    }

    // 이미지/동영상 구분
    public String determineFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("파일이 정의되지 않습니다.");
        }

        if (contentType.startsWith("image/")) {
            return "IMAGE";
        } else if (contentType.startsWith("video/")) {
            return "VIDEO";
        } else {
            throw new IllegalArgumentException("지원하지 않는 파일 타입: " + contentType);
        }
    }

    /**
     * S3 파일 키 생성
     * @param reviewId 파일 ID
     * @return 생성된 파일 키
     */
    private String generateFileKey(Long reviewId, AtomicInteger fileNum) {
        return "Review/" + reviewId + "_review" + fileNum.get(); // 확장자 하드코딩보단 그냥 빼는게 나은거같음
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


    /**
     * 특정 장소에 연결된 리뷰를 페이징하여 조회하는 메서드
     *
     * @param Id 조회할 장소 또는 펜션의 ID
     * @param type    장소 유형 ("시설" 또는 "펜션")
     * @param pageNum 조회할 페이지 번호 (0부터 시작)
     * @param page    한 페이지당 표시할 리뷰 수
     * @return 페이징 처리된 리뷰 리스트
     */
    @Transactional(readOnly = true)
    public List<Review> getReviews(Long Id, String type, int pageNum, int page) {
        Pageable pageable = PageRequest.of(pageNum, page);
        List<Review> reviews = reviewRepository.findReviewsByPlaceId(Id, type, pageable);

        if (reviews.isEmpty()) {
            throw NotFoundException.entityNotFound("리뷰");
        }
        return reviews;

    }


    /**
     * 리뷰를 바탕으로 ReviewSummaryResponseDto 리스트를 생성하는 메서드
     * @param reviews 리뷰 리스트
     * @return ReviewSummaryResponseDto 리스트
     */
    public List<ReviewSummaryResponseDto> getReviewSummaryResponseDtoList(List<Review> reviews) {
        return reviews.stream()
                .map(review -> ReviewSummaryResponseDto.builder()
                        .reviewId(review.getReviewId())
                        .profileImageUrl((review.getMember() != null && review.getMember().getProfileImage() != null)
                                ? review.getMember().getProfileImage().getFileUrl()
                                : null) // Profile 이미지가 별도로 필요하면 추가
                        .content(review.getContent())
                        .score(review.getScore().doubleValue())
                        .visitDate(review.getVisitDate().toString())
                        .nickname(review.getNickname())
                        .file(review.getReviewFiles().stream()
                                .map(file -> ReviewSummaryFileDto.builder()
                                        .mediaFileId(file.getFile().getMediaFileId())
                                        .fileType(file.getFile().getFileType().name())
                                        .fileUrl(file.getFile().getFileUrl())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 특정 장소의 사진 리뷰 요약 리스트를 조회하는 메서드
     * @param placeId
     * @param type
     * @return PhotoReviewSummaryResponseDto 리스트
     */
    public List<PhotoReviewSummaryResponseDto> getPhotoReviewSummaries(Long placeId, String type) {
        return reviewRepository.findPhotoReviewSummaries(placeId, type).orElseThrow(() -> NotFoundException.entityNotFound("리뷰 요약 리스트"));
    }

    /**
     * 리뷰 정보를 조회하고 DTO로 변환하여 반환하는 메서드.
     *
     * @param type 리뷰 타입 (예: 긍정, 부정 등)
     * @param placePensionId 장소나 펜션 ID
     * @param pageable 페이징 정보를 담은 Pageable 객체
     * @return 변환된 ReviewSummaryResponseDto 객체의 Slice
     */
    @Transactional(readOnly = true)
    public Slice<ReviewSummaryResponseDto> getReviews(String type, Long placePensionId, Pageable pageable) {
        // 1단계: 부모 엔티티 페이징
        Slice<Review> reviews = reviewRepository.findByTypeAndPlacePensionId(type, placePensionId, pageable)
                .orElseThrow(() -> NotFoundException.entityNotFound("리뷰"));

        // 리뷰 ID 목록 추출
        List<Long> reviewIds = reviews.getContent().stream()
                .map(Review::getReviewId)
                .collect(Collectors.toList());

        // 2단계: 연관 데이터 로드 (ReviewFile)
        List<ReviewFile> reviewFiles = reviewFileRepository.findFilesByReviewIds(reviewIds)
                .orElseThrow(() -> NotFoundException.entityNotFound("리뷰 파일"));

        Map<Long, List<ReviewSummaryFileDto>> fileMap = reviewFiles.stream()
                .collect(Collectors.groupingBy(
                        file -> file.getReview().getReviewId(),
                        Collectors.mapping(ReviewSummaryFileDto::from, Collectors.toList())
                ));

        // 3단계: 연관 데이터 로드 (Member)
        List<Long> memberIds = reviews.getContent().stream()
                .map(review -> review.getMember().getMemberId())
                .distinct()
                .collect(Collectors.toList());

        List<Member> members = memberRepository.findAllById(memberIds);
        Map<Long, Member> memberMap = members.stream()
                .collect(Collectors.toMap(Member::getMemberId, Function.identity()));

        // DTO 변환
        return reviews.map(review -> {
            Member member = memberMap.get(review.getMember().getMemberId());
            return ReviewSummaryResponseDto.from(review, fileMap.getOrDefault(review.getReviewId(), List.of()), member);
        });
    }



}
