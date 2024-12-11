package com.meong9.backend.domain.recommendation.recommendation.service;

import com.meong9.backend.domain.address.entity.PlcPenAddress;
import com.meong9.backend.domain.address.repository.PlcPenAddressRepository;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.member.repository.MemberRepository;
import com.meong9.backend.domain.pension.entity.PensionFile;
import com.meong9.backend.domain.place.entity.PlaceFile;
import com.meong9.backend.domain.recommendation.recommendation.dto.RecommendationDto;
import com.meong9.backend.domain.recommendation.recommendation.entity.PensionRecommendation;
import com.meong9.backend.domain.recommendation.recommendation.entity.PlaceRecommendation;
import com.meong9.backend.domain.recommendation.recommendation.repository.PensionRecommendationRepository;
import com.meong9.backend.domain.recommendation.recommendation.repository.PlaceRecommendationRepository;
import com.meong9.backend.domain.review.repository.ReviewRepository;
import com.meong9.backend.global.exception.NotFoundException;
import com.meong9.backend.global.utils.AddressMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.impl.model.jdbc.ReloadFromJDBCDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RecommendationService {

    @Qualifier("pensionDataModel")
    private final DataModel pensionDataModel;

    @Qualifier("placeDataModel")
    private final DataModel placeDataModel;

    private final UserBasedRecommendation userBasedRecommendation;
    private final ItemBasedRecommendation itemBasedRecommendation;
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final PensionRecommendationRepository pensionRecommendationRepository;
    private final PlaceRecommendationRepository placeRecommendationRepository;
    private final PlcPenAddressRepository plcPenAddressRepository;

    public RecommendationService(@Qualifier("pensionDataModel")DataModel pensionDataModel, @Qualifier("placeDataModel")DataModel placeDataModel,
                                 UserBasedRecommendation userBasedRecommendation, ItemBasedRecommendation itemBasedRecommendation,
                                 ReviewRepository reviewRepository, MemberRepository memberRepository,
                                 PensionRecommendationRepository pensionRecommendationRepository, PlaceRecommendationRepository placeRecommendationRepository, PlcPenAddressRepository plcPenAddressRepository) {
        this.pensionDataModel = pensionDataModel;
        this.placeDataModel = placeDataModel;
        this.userBasedRecommendation = userBasedRecommendation;
        this.itemBasedRecommendation = itemBasedRecommendation;
        this.reviewRepository = reviewRepository;
        this.memberRepository = memberRepository;
        this.pensionRecommendationRepository = pensionRecommendationRepository;
        this.placeRecommendationRepository = placeRecommendationRepository;
        this.plcPenAddressRepository = plcPenAddressRepository;
    }

    // 사용자 기반 펜션 추천 (User-Based)
    @Transactional(readOnly = true)
    public List<RecommendedItem> processUserRecommendations(Long userId) {
        log.info("Processing recommendations for userId: {}", userId); // 로그 추가
        try {
            if (pensionDataModel instanceof ReloadFromJDBCDataModel) {
                ReloadFromJDBCDataModel model = (ReloadFromJDBCDataModel) pensionDataModel;
                model.refresh(null); // 데이터 새로고침

                log.info("DataModel 새로고침.");
            }
            return userBasedRecommendation.recommend(pensionDataModel, userId, 10);
        } catch (Exception e) {
            log.error("Error processing user recommendations for userId: {}", userId, e);
            return List.of();
        }
    }


    // 특정 펜션에 대한 시설 추천 생성 (Item-Based)
    @Transactional(readOnly = true)
    public List<RecommendedItem> recommendFacilitiesForPension(Long pensionId) {
        try {
            return itemBasedRecommendation.recommend(placeDataModel, pensionId, 10);
        } catch (Exception e) {
            log.error("Error recommending facilities for pensionId: {}", pensionId, e);
            return List.of();
        }
    }

    // 리뷰가 1개 이상 달린 펜션 ID 조회
    @Transactional(readOnly = true)
    public List<Long> getPensionsWithReviews() {
        log.info("로그 1개 이상 달린 펜션 ID");
        return reviewRepository.findPensionReviewCount(1);
    }

    // 활성 사용자 ID 조회
    @Transactional(readOnly = true)
    public List<Long> getActiveMemberIds() {
        LocalDateTime week = LocalDateTime.now().minusWeeks(1); // 일주일 간 접속한 사람만
        log.info("일주일 간 접속한 사용자");
        List<Long> memberIds = memberRepository.findActiveMembers(week);
        log.info("Retrieved active member IDs: {}", memberIds);
        return memberIds;
    }

    // 추천 테이블 아이템 반환 (펜션)
    @Transactional(readOnly = true)
    public List<RecommendationDto> getPensionRecommendations(Member member, int maxItems) {
        List<PensionRecommendation> recommendations = pensionRecommendationRepository.findByMemberId(member.getMemberId());

        // recommendations의 크기가 maxItems보다 작거나 같으면 전체 반환
        if (recommendations.size() > maxItems) {
            recommendations = recommendations.subList(0, maxItems);
        }

        List<RecommendationDto> pensionRecommendationList = new ArrayList<>();

        for(PensionRecommendation pensionRecommendation : recommendations){
            PlcPenAddress plcPenAddress= plcPenAddressRepository.findByPlcPenIdAndType(pensionRecommendation.getPensionMemberId().getPensionId(), "020")
                    .orElseThrow(() -> NotFoundException.entityNotFound("시설 주소"));

            String address = AddressMapper.formatAddress(plcPenAddress);

            // 이미지 null 체크
            String img = null;
            if (pensionRecommendation.getPension() != null &&
                    pensionRecommendation.getPension().getPensionFiles() != null &&
                    !pensionRecommendation.getPension().getPensionFiles().isEmpty()) {
                PensionFile pensionFile = pensionRecommendation.getPension().getPensionFiles().get(0); // 대표 이미지 한장
                if (pensionFile != null && pensionFile.getMediaFile() != null) {
                    img = pensionFile.getMediaFile().getFileUrl();
                }
            }

            // 리뷰 null 체크
            Double reviewAvg = pensionRecommendation.getPension() != null ? pensionRecommendation.getPension().getReviewAvg() : null;
            int reviewCount = pensionRecommendation.getPension() != null ? pensionRecommendation.getPension().getReviewCount() : 0;
            String formattedReviewAvg = (reviewAvg != null) ? new DecimalFormat("#.#").format(reviewAvg) : "0.0";

            RecommendationDto pensionRecommendationDto = RecommendationDto.builder()
                    .id(pensionRecommendation.getPensionMemberId().getPensionId())
                    .name(pensionRecommendation.getPension().getName())
                    .address(address)
                    .img(img)
                    .reviewAvg(formattedReviewAvg)
                    .reviewCount(reviewCount)
                    .build();

            pensionRecommendationList.add(pensionRecommendationDto);
        }
        return pensionRecommendationList;
    }

    // 추천 테이블 아이템 반환 (시설)
    @Transactional(readOnly = true)
    public List<RecommendationDto> getPlacecommendations(Long pensionId, int maxItems) {
        List<PlaceRecommendation> recommendations = placeRecommendationRepository.findByPensionId(pensionId);


        // recommendations의 크기가 maxItems보다 작거나 같으면 전체 반환
        if (recommendations.size() > maxItems) {
            recommendations = recommendations.subList(0, maxItems);
        }

        List<RecommendationDto> placeRecommendationList = new ArrayList<>();

        for(PlaceRecommendation placeRecommendation : recommendations){
            PlcPenAddress plcPenAddress= plcPenAddressRepository.findByPlcPenIdAndType(placeRecommendation.getPlace().getPlaceId(), "010")
                    .orElseThrow(() -> NotFoundException.entityNotFound("펜션 주소"));

            // 주소 null 체크
            String address = "";
            if (plcPenAddress.getAddress() != null) {
                address = AddressMapper.formatAddress(plcPenAddress);
            }

            // 이미지 null 체크
            String img = "";
            if (placeRecommendation.getPlace() != null) {
                List<PlaceFile> placeFiles = placeRecommendation.getPlace().getPlaceFiles();
                if (placeFiles != null && !placeFiles.isEmpty()) { // 리스트 null 및 비어있는지 체크
                    PlaceFile placeFile = placeFiles.get(0); // 첫 번째 파일 가져오기
                    if (placeFile != null && placeFile.getMediaFile() != null) {
                        img = placeFile.getMediaFile().getFileUrl();
                        log.info("이미지가 존재합니다. MediaFileId: {}", placeFile.getMediaFile().getMediaFileId());
                    }
                } else {
                    log.info("PlaceFiles가 비어있습니다.");
                }
            } else {
                log.info("PlaceRecommendation의 Place가 null입니다.");
            }

            // 리뷰 null 체크
            Double reviewAvg = placeRecommendation.getPlace() != null ? placeRecommendation.getPlace().getReviewAvg() : null;
            int reviewCount = placeRecommendation.getPlace() != null ? placeRecommendation.getPlace().getReviewCount() : 0;
            String formattedReviewAvg = (reviewAvg != null) ? new DecimalFormat("#.#").format(reviewAvg) : "0.0";


            RecommendationDto placeRecommendationDto = RecommendationDto.builder()
                    .id(placeRecommendation.getPlace().getPlaceId())
                    .name(placeRecommendation.getPlace().getName())
                    .address(address)
                    .img(img)
                    .reviewAvg(formattedReviewAvg)
                    .reviewCount(reviewCount)
                    .build();

            placeRecommendationList.add(placeRecommendationDto);
        }
        return placeRecommendationList;
    }

}
