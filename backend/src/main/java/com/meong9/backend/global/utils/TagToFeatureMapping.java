package com.meong9.backend.global.utils;

import com.meong9.backend.global.topFeature.entity.TopFeature;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
        TagToFeatureMapping mapping = mappingMap.get(tagId); // O(1) 검색
        if (mapping != null) {
            mapping.featureSetter.accept(topFeature);
        }
    }
}
