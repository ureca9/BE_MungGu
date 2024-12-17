package com.meong9.backend.global.batch.pension.processor;

import com.meong9.backend.domain.pension.entity.TopPension;
import com.meong9.backend.global.batch.pension.dto.RedisTopPensionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class TopPensionProcessor implements ItemProcessor<RedisTopPensionDto, TopPension> {

    @Override
    public TopPension process(RedisTopPensionDto dto) {
        LocalDate today = LocalDate.now();

        return TopPension.builder()
                .pensionId(dto.getPensionId())
                .pensionName(dto.getPensionName())
                .reviewCount(dto.getReviewCount())
                .reviewAvg(BigDecimal.valueOf(dto.getReviewAvg() != null ? dto.getReviewAvg() : null))
                .likeCount(dto.getLikeCount())
                .province(dto.getProvince())
                .cityDistrict(dto.getCityDistrict())
                .subDistrict(dto.getSubDistrict())
                .viewCount(dto.getScore().longValue())
                .roomPriceAvg(dto.getRoomPriceAvg())
                .year(today.getYear())
                .month(today.getMonthValue())
                .date(today.getDayOfMonth())
                .build();
    }
}
