package com.meong9.backend.global.batch.place.processor;

import com.meong9.backend.domain.place.entity.TopPlace;
import com.meong9.backend.global.batch.place.dto.RedisTopPlaceDto;
import com.meong9.backend.global.batch.place.dto.TopPlaceAndFeature;
import com.meong9.backend.global.topFeature.entity.TopFeature;
import com.meong9.backend.global.utils.TagToFeatureMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TopPlaceAndFeatureProcessor implements ItemProcessor<RedisTopPlaceDto, TopPlaceAndFeature> {

    @Override
    public TopPlaceAndFeature process(RedisTopPlaceDto dto) {
        LocalDate today = LocalDate.now();

        // TopPlace 객체 생성
        TopPlace topPlace = TopPlace.builder()
                .placeId(dto.getPlaceId())
                .placeName(dto.getPlaceName())
                .reviewCount(dto.getReviewCount())
                .reviewAvg(BigDecimal.valueOf(dto.getReviewAvg() != null ? dto.getReviewAvg() : 0))
                .likeCount(dto.getLikeCount())
                .category(dto.getCategory())
                .province(dto.getProvince())
                .cityDistrict(dto.getCityDistrict())
                .subDistrict(dto.getSubDistrict())
                .viewCount(dto.getScore().longValue())
                .rank(dto.getRank())
                .year(today.getYear())
                .month(today.getMonthValue())
                .date(today.getDayOfMonth())
                .placeFeatures(new ArrayList<>())
                .build();

        // TopFeature 생성
        TopFeature topFeature = createTopFeatureFromTags(dto.getTagIds());

        return new TopPlaceAndFeature(topPlace, topFeature);
    }


    /**
     * 주어진 태그 ID 리스트를 기반으로 TopFeature 객체를 생성합니다.
     *
     * @param tagIds 태그 ID 리스트
     * @return 생성된 TopFeature 객체
     */
    private TopFeature createTopFeatureFromTags(List<Long> tagIds) {
        TopFeature topFeature = new TopFeature();

        if (tagIds == null || tagIds.isEmpty()) {
            System.out.println("Warning: No tag IDs provided. Skipping TopFeature creation.");
            return topFeature;
        }

        for (Long tagId : tagIds) {
            TagToFeatureMapping.applyFeature(tagId, topFeature);
        }
        return topFeature;
    }

}
