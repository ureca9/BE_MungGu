package com.meong9.backend.domain.recommendation.recommendation.service;

import com.meong9.backend.domain.address.entity.PlcPenAddress;
import com.meong9.backend.domain.address.repository.PlcPenAddressRepository;
import com.meong9.backend.domain.like.service.LikeService;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.member.repository.MemberRepository;
import com.meong9.backend.domain.pension.entity.PensionFile;
import com.meong9.backend.domain.place.entity.PlaceFile;
import com.meong9.backend.domain.recommendation.id_class.PensionPlaceId;
import com.meong9.backend.domain.recommendation.recommendation.dto.RecommendationDto;
import com.meong9.backend.domain.recommendation.recommendation.entity.PensionRecommendation;
import com.meong9.backend.domain.recommendation.recommendation.entity.PlaceRecommendation;
import com.meong9.backend.domain.recommendation.recommendation.projection.PlcPenProjection;
import com.meong9.backend.domain.recommendation.recommendation.repository.PensionRecommendationRepository;
import com.meong9.backend.domain.recommendation.recommendation.repository.PlaceRecommendationRepository;
import com.meong9.backend.domain.review.repository.ReviewRepository;
import com.meong9.backend.global.exception.NotFoundException;
import com.meong9.backend.global.utils.AddressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.impl.model.jdbc.ReloadFromJDBCDataModel;
import org.apache.mahout.cf.taste.impl.recommender.GenericRecommendedItem;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationService {
    private final UserBasedRecommendation userBasedRecommendation;
    private final ItemBasedRecommendation itemBasedRecommendation;
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final PensionRecommendationRepository pensionRecommendationRepository;
    private final PlaceRecommendationRepository placeRecommendationRepository;
    private final PlcPenAddressRepository plcPenAddressRepository;
    private final ContentBasedRecommendation contentBasedRecommendation;
    private final LikeService likeService;

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

    // 추천 테이블 아이템 조회 (펜션)
    @Transactional(readOnly = true)
    public List<RecommendationDto> getPensionRecommendations(Member member, int maxItems) {
        List<PensionRecommendation> recommendations = pensionRecommendationRepository.findByMemberId(member.getMemberId());

        // 추천 목록 생성
        List<RecommendationDto> pensionRecommendationList = new ArrayList<>();

        for (PensionRecommendation pensionRecommendation : recommendations) {
            PlcPenAddress plcPenAddress = plcPenAddressRepository.findByPlcPenIdAndType(pensionRecommendation.getPensionMemberId().getPensionId(), "020")
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

        // 추천 목록이 maxItems보다 적으면 인기 펜션 추가
        if (pensionRecommendationList.size() < maxItems) {
            int count = maxItems - pensionRecommendationList.size();
            List<RecommendationDto> popularPensions = likeService.getTopLikedPensions(count);

            for(int i=0;i<count;i++) {
                pensionRecommendationList.add(popularPensions.get(i));
            }
        }

        return pensionRecommendationList;
    }

    // 추천 테이블 아이템 조회 (시설)
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

    /**
     * 추천
     * @param targetId : 추천 타겟 값 (사용자 펜션 추천에서는 사용자 Id, 펜션 관련 시설 추천에서는 펜션 Id)
     * @param allItems : 추천 탐색을 할 아이템들 (사용자 펜션 추천에서는 펜션 ID, 펜션 관련 시설 추천에서는 시설 ID)
     * @param type : 펜션인지 시설인지 구분 여부 (사용자 펜션 추천인지, 펜션 관련 시설 추천인지 구분)
     * @param placeIdsByProvince : province 별로 place id 값을 가지고 있는 Map
     * @return
     */
    public List<RecommendedItem> recommend(Long targetId, List<Long> allItems, String type, Map<String, List<PlcPenProjection>> placeIdsByProvince, PlcPenProjection pensionInfo) {
        log.info("추천 계산 시작 (targetId: {}, type: {})", targetId, type);

        // 1. 점수 계산
        Map<Long, Double> combinedScores = calculateScores(targetId, allItems, type, placeIdsByProvince, pensionInfo);

        // 2. 상위 추천 항목 반환
        return getTopRecommendations(combinedScores, 5);
    }

    /**
     * Item 기반 추천 (recommend 전 데이터 처리)
        @Param pensionIds : pension_place_score에 존재하는 pensionId들
        @Param placeIdsByProvince : province 별로 place id 값을 가지고 있는 Map
        @Param allPensionIds : 존재하는 모든 pensionId
        @Param pensionId : 현재 추천 타겟 id
        @Param type : 펜션인지 시설인지 구분 여부 (사용자 펜션 추천인지, 펜션 관련 시설 추천인지 구분)
     */
    public List<RecommendedItem> itemRecommend(List<Long> placeIds, Map<String, List<PlcPenProjection>> placeIdsByProvince,
                                                 Long pensionId, String type, PlcPenProjection pensionInfo){

        return recommend(pensionId, placeIds, type, placeIdsByProvince, pensionInfo);
    }

    private Map<Long, Double> calculateScores(Long targetId, List<Long> allItems, String type, Map<String, List<PlcPenProjection>> placeIdsByProvince, PlcPenProjection pensionInfo) {

        Map<Long, Double> collaborativeScores = new HashMap<>();
        Map<Long, Double> contentScores = new HashMap<>();
        if(type.equals("Pension")){
            // 협업 필터링 점수 계산
            collaborativeScores = userBasedRecommendation.processUserRecommendations(targetId);
            log.info("user base 협업 필터링 점수: {}", collaborativeScores);

            // 콘텐츠 기반 점수 계산
            contentScores = contentBasedRecommendation.calculateContentScores(targetId, allItems, type);
            log.info("user base 콘텐츠 기반 점수: {}", contentScores);
        } else if(type.equals("Place")){
            // 협업 필터링 점수 계산
            collaborativeScores = itemBasedRecommendation.recommendFacilitiesForPension(targetId);
            log.info("item base 협업 필터링 점수: {}", collaborativeScores);

            // 콘텐츠 기반 점수 계산
            contentScores = contentBasedRecommendation.calculateContentScoresByDistance(placeIdsByProvince, pensionInfo);
            log.info("item base 콘텐츠 기반 점수: {}", contentScores);
        }

        // 점수 결합
        return combineScores(collaborativeScores, contentScores, 0.8);
    }

    private List<RecommendedItem> getTopRecommendations(Map<Long, Double> combinedScores, int limit) {
        // 점수 기준으로 정렬 (내림차순)
        List<Map.Entry<Long, Double>> sortedScores = combinedScores.entrySet().stream()
                .filter(entry -> entry.getValue() > 0) // 점수가 0보다 큰 항목만 포함
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .collect(Collectors.toList());

//        log.info("정렬된 점수 목록 (0점 제외): {}", sortedScores);

        List<RecommendedItem> recommendations = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : sortedScores) {
            if (recommendations.size() >= limit) break;

            long itemId = entry.getKey();
            double originalScore = entry.getValue();
            float convertedScore = (float) originalScore;

//            log.info("아이템 ID: {}, 원래 점수 (double): {}, 변환된 점수 (float): {}", itemId, originalScore, convertedScore);

            recommendations.add(new GenericRecommendedItem(itemId, convertedScore));
        }

        log.info("최종 추천 리스트 (Top {}): {}", limit, recommendations);
        return recommendations;
    }


    public static Map<Long, Double> combineScores(
            Map<Long, Double> collaborativeScores,
            Map<Long, Double> contentScores,
            double collaborativeWeight
    ) {
        double contentWeight = 1.0 - collaborativeWeight;
        Map<Long, Double> combinedScores = new HashMap<>();

        // 키의 합집합 생성
        Set<Long> allKeys = new HashSet<>();
        allKeys.addAll(collaborativeScores.keySet());
        allKeys.addAll(contentScores.keySet());

        // 모든 키에 대해 점수 결합
        for (Long id : allKeys) {
            double collaborativeScore = collaborativeScores.getOrDefault(id, 0.0);
            double contentScore = contentScores.getOrDefault(id, 0.0);

            // 최종 점수 계산
            double combinedScore = (collaborativeScore * collaborativeWeight) + (contentScore * contentWeight);
            if (combinedScore > 0) { // 0점 제외
                combinedScores.put(id, combinedScore);
            }


//            log.info("ID: {}, Collaborative Score: {}, Content Score: {}, Combined Score: {}",
//                    id, collaborativeScore, contentScore, combinedScore);
        }

        return combinedScores;
    }

}
