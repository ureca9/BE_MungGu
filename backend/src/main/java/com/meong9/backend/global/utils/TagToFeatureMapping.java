package com.meong9.backend.global.utils;

import com.meong9.backend.global.topFeature.entity.TopFeature;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
public enum TagToFeatureMapping {
    PARKING(1L, "주차 가능", topFeature -> topFeature.setParking(true)),
    PET_ONLY_AREA(2L, "반려동물 전용 구역", topFeature -> topFeature.setPetOnlyArea(true)),
    INDOOR_SPACE(3L, "실내 장소 여부", topFeature -> topFeature.setIndoorSpace(true)),
    OUTDOOR_SPACE(4L, "야외 장소 여부", topFeature -> topFeature.setOutdoorSpace(true)),
    WEIGHT_LIMIT(5L, "무게 제한 없음", topFeature -> topFeature.setWeightLimit(true)),
    SWIMMING_POOL(6L, "수영장 여부", topFeature -> topFeature.setSwimmingPool(true)),
    BARBECUE(7L, "바비큐 가능 여부", topFeature -> topFeature.setBarbecue(true)),
    BULMEONG(8L, "불멍 가능", topFeature -> topFeature.setBulmeong(true)),
    FENCE(9L, "울타리 있음", topFeature -> topFeature.setFence(true)),
    BARKING(10L, "짖어도 괜찮은지 여부", topFeature -> topFeature.setBarking(true)),
    NO_SMOKING(11L, "금연", topFeature -> topFeature.setNoSmoking(true));

    private final Long tagId;
    private final String tagName;
    private final Consumer<TopFeature> featureSetter;

    // 태그 ID를 키로, 매핑을 값으로 갖는 Map
    private static final Map<Long, TagToFeatureMapping> mappingMap =
            Arrays.stream(values())
                    .collect(Collectors.toMap(mapping -> mapping.tagId, mapping -> mapping));

    TagToFeatureMapping(Long tagId, String tagName, Consumer<TopFeature> featureSetter) {
        this.tagId = tagId;
        this.tagName = tagName;
        this.featureSetter = featureSetter;
    }

    public static void applyFeature(Long tagId, TopFeature topFeature) {
        if (tagId == null || topFeature == null) {
            throw new IllegalArgumentException("태그 ID와 TopFeature 객체는 null일 수 없습니다.");
        }

        TagToFeatureMapping mapping = mappingMap.get(tagId);
        if (mapping == null) {
            log.warn("알 수 없는 태그 ID: {}", tagId);
            return;
        }

        try {
            mapping.featureSetter.accept(topFeature);
            log.debug("피처 적용 완료: tagId={}, feature={}", tagId, mapping.tagName);
        } catch (Exception e) {
            log.error("피처 적용 실패: tagId={}, feature={}", tagId, mapping.tagName, e);
            throw new RuntimeException("피처 적용 중 오류 발생", e);
        }
    }
}
