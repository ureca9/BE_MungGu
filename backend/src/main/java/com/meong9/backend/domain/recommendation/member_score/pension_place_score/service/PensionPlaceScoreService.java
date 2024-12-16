package com.meong9.backend.domain.recommendation.member_score.pension_place_score.service;

import com.meong9.backend.domain.pension.entity.Pension;
import com.meong9.backend.domain.pension.repository.PensionRepository;
import com.meong9.backend.domain.place.entity.Place;
import com.meong9.backend.domain.place.repository.PlaceRepository;
import com.meong9.backend.domain.recommendation.id_class.PensionPlaceId;
import com.meong9.backend.domain.recommendation.member_score.pension_member_score.repository.PensionMemberScoreRepository;
import com.meong9.backend.domain.recommendation.member_score.pension_place_score.entity.PensionPlaceScore;
import com.meong9.backend.domain.recommendation.member_score.pension_place_score.repository.PensionPlaceScoreRepository;
import com.meong9.backend.domain.recommendation.member_score.place_member_score.repository.PlaceMemberScoreRepository;
import com.meong9.backend.domain.recommendation.recommendation.dto.PensionPlaceScoreDto;
import com.meong9.backend.domain.review.repository.ReviewRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PensionPlaceScoreService {

    private final PensionPlaceScoreRepository pensionPlaceScoreRepository;
    private final PlaceMemberScoreRepository placeMemberScoreRepository;
    private final PensionRepository pensionRepository;
    private final PlaceRepository placeRepository;
    private final ReviewRepository reviewRepository;
    private final PensionMemberScoreRepository pensionMemberScoreRepository;
    private final PensionPlaceScoreJdbcRepository pensionPlaceScoreJdbcRepository;
    @PersistenceContext
    private EntityManager entityManager;


    public void initializeAndUpdateScores() {
        log.info("pension_place_score 정보 저장 시작");

        // 1. JPA를 사용하여 데이터 로드 및 점수 계산
        List<PensionPlaceScore> scoresToSave = calculateScores();
        clearPersistenceContext();
        // 2. JDBC를 사용하여 데이터 저장
        batchSaveScores(scoresToSave);

        log.info("pension_place_score 저장 완료");
    }

    /**
     * JPA를 사용한 데이터 로드 및 점수 계산
     */
    @Transactional(readOnly = true)
    public List<PensionPlaceScore> calculateScores() {
        // 1. 리뷰가 있는 펜션과 시설 정보 로드
        List<Long> pensionIds = reviewRepository.findPensionReviewCount(1);
        List<Long> placeIds = placeMemberScoreRepository.findAllPlaceIds();

        // 2. 공통 멤버 조회
        Map<Long, Map<Long, List<Long>>> pensionPlaceMembers = findAllCommonMembers(pensionIds, placeIds);

        // 3. 멤버 점수 조회
        List<Long> allMemberIds = pensionPlaceMembers.values().stream()
                .flatMap(placeMap -> placeMap.values().stream())
                .flatMap(List::stream)
                .distinct()
                .toList();

        Map<Long, Float> pensionScores = convertToScoreMap(
                pensionMemberScoreRepository.findScoresBatch(allMemberIds, pensionIds)
        );
        Map<Long, Float> placeScores = convertToScoreMap(
                placeMemberScoreRepository.findScoresBatch(allMemberIds, placeIds)
        );

        // 4. 기존 데이터 로드
        Map<PensionPlaceId, PensionPlaceScore> existingScores = loadExistingScores(pensionPlaceMembers);

        // 5. 펜션과 시설 데이터 로드
        Map<Long, Pension> pensions = loadPensions(pensionIds);
        Map<Long, Place> places = loadPlaces(placeIds);

        // 6. 점수 계산
        List<PensionPlaceScore> scoresToSave = new ArrayList<>();
        processScores(pensionPlaceMembers, pensionScores, placeScores, pensions, places, existingScores, scoresToSave);

        log.info("총 저장할 PensionPlaceScores: {}", scoresToSave.size());
        return scoresToSave;
    }

    /**
     * JDBC를 사용한 배치 저장
     */
    private void batchSaveScores(List<PensionPlaceScore> scores) {
        // DTO 변환 (JDBC에서는 필요한 데이터만 추출)
        List<PensionPlaceScoreDto> scoreDtos = scores.stream()
                .map(score -> new PensionPlaceScoreDto(
                        score.getPensionPlaceId(),
                        score.getScore(),
                        score.getLastUpdatedAt()
                ))
                .toList();

        // JDBC Batch Insert or Update 실행
        pensionPlaceScoreJdbcRepository.batchInsertOrUpdate(scoreDtos);
    }

    private Map<Long, Map<Long, List<Long>>> findAllCommonMembers(List<Long> pensionIds, List<Long> placeIds) {
        List<Object[]> results = pensionPlaceScoreRepository.findCommonMembersBatch(pensionIds, placeIds);

        Map<Long, Map<Long, List<Long>>> pensionPlaceMembers = new HashMap<>();
        for (Object[] result : results) {
            Long pensionId = (Long) result[0];
            Long placeId = (Long) result[1];
            Long memberId = (Long) result[2];

            pensionPlaceMembers
                    .computeIfAbsent(pensionId, k -> new HashMap<>())
                    .computeIfAbsent(placeId, k -> new ArrayList<>())
                    .add(memberId);
        }
        return pensionPlaceMembers;
    }

    private void processScores(
            Map<Long, Map<Long, List<Long>>> pensionPlaceMembers,
            Map<Long, Float> pensionScores,
            Map<Long, Float> placeScores,
            Map<Long, Pension> pensions,
            Map<Long, Place> places,
            Map<PensionPlaceId, PensionPlaceScore> existingScores,
            List<PensionPlaceScore> scoresToSave
    ) {
        pensionPlaceMembers.forEach((pensionId, placeMembers) -> {
            placeMembers.forEach((placeId, commonMembers) -> {
                if (!commonMembers.isEmpty()) {
                    float score = calculateScore(commonMembers, pensionScores, placeScores);
                    Pension pension = pensions.get(pensionId);
                    Place place = places.get(placeId);

                    if (pension != null && place != null) {
                        PensionPlaceId pensionPlaceId = new PensionPlaceId(pensionId, placeId);
                        PensionPlaceScore pensionPlaceScore = existingScores.getOrDefault(
                                pensionPlaceId,
                                PensionPlaceScore.builder()
                                        .pensionPlaceId(pensionPlaceId)
                                        .pension(pension)
                                        .place(place)
                                        .lastUpdatedAt(LocalDateTime.now())
                                        .score(score)
                                        .build()
                        );

                        // 변경된 경우에만 추가
                        if (pensionPlaceScore.getScore() != score) {
                            pensionPlaceScore.setScore(score);
                            pensionPlaceScore.setLastUpdatedAt(LocalDateTime.now());
                            scoresToSave.add(pensionPlaceScore);
                        }
                    }
                }
            });
        });
    }


    private Map<Long, Float> convertToScoreMap(List<Object[]> results) {
        Map<Long, Float> scoreMap = new HashMap<>();
        for (Object[] result : results) {
            Long memberId = (Long) result[0];
            Float score = ((Number) result[1]).floatValue();
            scoreMap.put(memberId, score);
        }
        return scoreMap;
    }

    private Map<Long, Pension> loadPensions(List<Long> pensionIds) {
        return pensionRepository.findAllByIdIn(pensionIds).stream()
                .collect(HashMap::new, (map, pension) -> map.put(pension.getPensionId(), pension), HashMap::putAll);
    }

    private Map<Long, Place> loadPlaces(List<Long> placeIds) {
        return placeRepository.findAllByIdIn(placeIds).stream()
                .collect(HashMap::new, (map, place) -> map.put(place.getPlaceId(), place), HashMap::putAll);
    }

    private Map<PensionPlaceId, PensionPlaceScore> loadExistingScores(
            Map<Long, Map<Long, List<Long>>> pensionPlaceMembers
    ) {
        Set<PensionPlaceId> ids = new HashSet<>();
        pensionPlaceMembers.forEach((pensionId, placeMap) -> placeMap.keySet()
                .forEach(placeId -> ids.add(new PensionPlaceId(pensionId, placeId))));

        return pensionPlaceScoreRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(PensionPlaceScore::getPensionPlaceId, score -> score));
    }

    // 점수 계산
    private float calculateScore(
            List<Long> commonMembers,
            Map<Long, Float> pensionScores,
            Map<Long, Float> placeScores
    ) {
        float totalScore = 0.0f; // 점수 총합 초기화
        for (Long memberId : commonMembers) {
            // 각 멤버의 펜션 점수, 시설 점수 조회
            float pensionScore = pensionScores.getOrDefault(memberId, 0.0f);
            float placeScore = placeScores.getOrDefault(memberId, 0.0f);

            // 펜션 점수와 시설 점수의 평균을 계산하여 총합에 더함
            totalScore += (pensionScore + placeScore) / 2;
        }

        // 공통 멤버 수로 나눠 최종 평균 점수 계산
        return totalScore / commonMembers.size();
    }

    @Transactional(readOnly = true)
    public List<Long> getPensionIds() {
        return pensionPlaceScoreRepository.findPensionIds();
    }

    /**
     * JPA 영속성 컨텍스트 초기화
     */
    @Transactional
    public void clearPersistenceContext() {
        entityManager.clear();
    }
}
