package com.meong9.backend.domain.review.service;


import com.meong9.backend.domain.address.entity.PlcPenAddress;
import com.meong9.backend.domain.address.repository.PlcPenAddressRepository;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.member.repository.MemberRepository;
import com.meong9.backend.domain.pension.entity.Pension;
import com.meong9.backend.domain.pension.repository.PensionRepository;
import com.meong9.backend.domain.place.entity.Place;
import com.meong9.backend.domain.place.repository.PlaceRepository;
import com.meong9.backend.domain.recommendation.member_score.MemberScoreService;
import com.meong9.backend.domain.review.dto.*;
import com.meong9.backend.domain.review.entity.Review;
import com.meong9.backend.domain.review.entity.ReviewFile;
import com.meong9.backend.domain.review.entity.id.ReviewFileId;
import com.meong9.backend.domain.review.repository.ReviewFileRepository;
import com.meong9.backend.domain.review.repository.ReviewRepository;
import com.meong9.backend.global.banword.inspector.BanWordInspector;
import com.meong9.backend.global.exception.AuthorizationException;
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
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
    private final BanWordInspector banWordInspector;
    private final MemberScoreService memberScoreService;


    @Transactional(readOnly = true)
    public ReviewDetailsResponseDto getReviewDetails(Long reviewId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()->NotFoundException.entityNotFound("리뷰"));

        return ReviewDetailsResponseDto.from(review);
    }

    @Transactional(readOnly = true)
    public List<MyReviewResponseDto> getMyReviews(Member member) {
        List<MyReviewResponseDto> reviews = reviewRepository.findReviewsByMember(member);

        return reviews;
    }

    @Transactional(readOnly = true)
    public Object getPlacePensionInfo(String type,Long plcPenId) {

        String fullAddress=plcPenAddressRepository.findFullAddress(type, plcPenId)
                .orElseThrow(() -> NotFoundException
                        .entityNotFound("찾으시는 주소가 없습니다, type: " +type+", id: " + plcPenId));

        if(Objects.equals(type, "010")){ // 장소
            ReviewInfoQueryResult queryResult=placeRepository.findByPlaceIdWithImageAndReviewCount(plcPenId,type)
                    .orElseThrow(() -> NotFoundException
                    .entityNotFound("type: " +type+", place_id: " + plcPenId));
            Place place = queryResult.getPlace();
            Integer reviewCount =  Math.toIntExact(queryResult.getReviewCount());
            String fileUrl=null;
            if(!place.getPlaceFiles().isEmpty()) {
                fileUrl=place.getPlaceFiles().get(0).getMediaFile().getFileUrl(); // 0번째 사진 가져오기
            }
            return new PlacePensionInfoResponseDto.PlaceResponse(place.getName(),fullAddress,place.getReviewAvg(),reviewCount,fileUrl,place.getLikeCount());
        }

        if(Objects.equals(type, "020")){ // 펜션
            ReviewInfoQueryResult queryResult=pensionRepository.findByPensionIdWithImageAndReviewCount(plcPenId,type)
                    .orElseThrow(() -> NotFoundException
                            .entityNotFound("type: " +type+", pension_id: " + plcPenId));
            Pension pension = queryResult.getPension();
            Integer reviewCount =  Math.toIntExact(queryResult.getReviewCount());

            String fileUrl=null;
            if(!pension.getPensionFiles().isEmpty()) {
                fileUrl=pension.getPensionFiles().get(0).getMediaFile().getFileUrl(); // 0번째 사진 가져오기
            }

            return new PlacePensionInfoResponseDto.PensionResponse(pension.getName(),fullAddress,pension.getReviewAvg(),reviewCount,fileUrl,pension.getLikeCount());
        }
        return null;
    }

    @Transactional
    public void createReview(ReviewRequestDto reviewRequestDto, List<MultipartFile> files, Member member) {
        List<MediaFile> mediaFiles = new ArrayList<>();
        Review review = Review.builder()
                .member(member)
                .content(banWordInspector.mask(reviewRequestDto.getContent(),"멍멍"))
                .nickname(member.getNickname())
                .score(reviewRequestDto.getScore())
                .type(reviewRequestDto.getType())
                .placePensionId(reviewRequestDto.getPlcPenId())
                .visitDate(reviewRequestDto.getVisitDate())
                .reviewFiles(null)
                .build();

        Review savedReview = reviewRepository.save(review);

        if(Objects.equals(reviewRequestDto.getType(), "010")){
            Place place=placeRepository.findById(savedReview.getPlacePensionId()).orElseThrow(() -> NotFoundException.entityNotFound("장소"));
            place.increaseReviewCount();
        }
        if(Objects.equals(reviewRequestDto.getType(), "020")){
            Pension pension=pensionRepository.findById(savedReview.getPlacePensionId()).orElseThrow(() -> NotFoundException.entityNotFound("펜션"));
            pension.increaseReviewCount();
        }
        processFileAsync(files, savedReview, mediaFiles);

        memberScoreService.addReview(member.getMemberId(), reviewRequestDto.getPlcPenId(), reviewRequestDto.getScore(), reviewRequestDto.getType());
    }

    @Transactional
    public void updateReview(Long reviewId, ReviewRequestDto reviewRequestDto, List<MultipartFile> files, Member member) {
        // 기존 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> NotFoundException.entityNotFound("리뷰"));

        // 작성자 권한 확인
        if (!review.getMember().equals(member)) {
            throw AuthorizationException.unauthorizedReviewUpdate("수정");
        }

        // 기존 파일 삭제
        List<ReviewFile> existingFiles = reviewFileRepository.findByReview(review);
        for (ReviewFile reviewFile : existingFiles) {
            reviewFileRepository.delete(reviewFile);
            reviewFile.getFile().delete(); // mediafile 소프트 삭제
            mediaFileService.deleteFromS3(reviewFile.getFile().getFileKey()); // S3에서 파일 삭제
        }
        Float oldScore = review.getScore(); // 예전 점수
        Float newScore = reviewRequestDto.getScore(); // 최신 점수

        review.update(reviewRequestDto);

        // 새로운 파일 처리
        List<MediaFile> mediaFiles = new ArrayList<>();
        processFileAsync(files, review, mediaFiles);

        memberScoreService.updateReview(member.getMemberId(), reviewRequestDto.getPlcPenId(), oldScore, newScore, reviewRequestDto.getType());
    }


    @Async // 비동기 실행
    public CompletableFuture<Void> processFileAsync(List<MultipartFile> files, Review review, List<MediaFile> mediaFiles) {
        try {
            processFileWithRetry(files, review, mediaFiles); // 재시도 로직 호출
        } catch (Exception e) {
            // 에러 처리
            log.error("파일 처리 중 오류 발생: {}", e.getMessage(), e);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Retryable( // 재시도 로직
            value = TimeoutException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    protected void processFileWithRetry(List<MultipartFile> files, Review review, List<MediaFile> mediaFiles) throws IOException, TimeoutException, InterruptedException {
        if (files != null) {
            List<ReviewFile> reviewFiles = new ArrayList<>();
            AtomicInteger fileNum = new AtomicInteger(0);
            for (MultipartFile mf : files) {
                MediaFile file = handleFileUpload(mf, review.getReviewId(), fileNum);
                mediaFiles.add(file);

                // 복합 키 생성
                ReviewFileId reviewFileId = new ReviewFileId(review.getReviewId(), file.getMediaFileId());

                // 객체가 이미 존재하면 가져오고, 없으면 새로 생성
                ReviewFile reviewFile = reviewFileRepository.findById(reviewFileId)
                        .orElseGet(() ->
                                ReviewFile.builder()
                                        .review(review)
                                        .file(file)
                                        .reviewFileId(reviewFileId)
                                        .build()
                        );

                reviewFiles.add(reviewFile);
                reviewFileRepository.save(reviewFile);
                fileNum.getAndIncrement();
            }
        }

        synchronizeTransaction(mediaFiles); // 트랜잭션 동기화 처리
    }

    /**
     * 파일(이미지, 동영상) 업로드 처리
     * @param file 업로드할 파일
     * @param reviewId 리뷰 id
     * @return 저장된 MediaFile 엔티티
     * @throws IOException 이미지 처리 오류
     */
    private MediaFile handleFileUpload(MultipartFile file, Long reviewId, AtomicInteger fileNum) throws IOException, InterruptedException, TimeoutException {
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

    private void synchronizeTransaction(List<MediaFile> mediaFiles){
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
    public void deleteReview(Long reviewId, Member member) throws IllegalAccessException {
        // 리뷰 조회 및 권한 확인
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("리뷰"));

        if (!review.getMember().equals(member)) {
            throw AuthorizationException.unauthorizedReviewUpdate("삭제");
        }

        // 연관된 파일 삭제
        if (review.getReviewFiles() != null) {
            for (ReviewFile reviewFile : review.getReviewFiles()) {
                mediaFileService.deleteFromS3(reviewFile.getFile().getFileKey());
                reviewFile.getFile().delete(); // mediafile 소프트 삭제
            }
        }

        // 리뷰 삭제 (ReviewFile은 CascadeType.ALL로 자동 삭제)
        reviewRepository.delete(review);

        // 리뷰 개수 감소
        if(Objects.equals(review.getType(), "010")){
            Place place=placeRepository.findById(review.getPlacePensionId()).orElseThrow(() -> NotFoundException.entityNotFound("장소"));
            place.decreaseReviewCount();
        }
        if(Objects.equals(review.getType(), "020")){
            Pension pension=pensionRepository.findById(review.getPlacePensionId()).orElseThrow(() -> NotFoundException.entityNotFound("펜션"));
            pension.decreaseReviewCount();
        }
        memberScoreService.deleteReview(member.getMemberId(), review.getPlacePensionId(), review.getScore(), review.getType());
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

    private MediaFile saveVideo(MultipartFile video, String fileKey) throws IOException, InterruptedException, TimeoutException {
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
     * 특정 장소의 사진 리뷰 요약 리스트를 조회하는 메서드
     * @param placeId
     * @param type
     * @return PhotoReviewSummaryResponseDto 리스트
     */
    @Transactional(readOnly = true)
    public Slice<PhotoReviewSummaryResponseDto> getPhotoReviewSummaryResponseDtoList(Long placeId, String type, Pageable pageable) {
        return reviewRepository.findPhotoReviewSummaries(placeId, type, pageable);
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
        Slice<Review> reviews = reviewRepository.findByTypeAndPlacePensionId(type, placePensionId, pageable);

        // 리뷰 ID 목록 추출
        List<Long> reviewIds = reviews.getContent().stream()
                .map(Review::getReviewId)
                .collect(Collectors.toList());

        // 2단계: 연관 데이터 로드 (ReviewFile)
        List<ReviewFile> reviewFiles = reviewFileRepository.findFilesByReviewIds(reviewIds);

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

