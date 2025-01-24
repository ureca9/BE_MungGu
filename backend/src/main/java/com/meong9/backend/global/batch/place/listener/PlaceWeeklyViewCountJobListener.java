package com.meong9.backend.global.batch.place.listener;

import com.meong9.backend.global.utils.CategoryMapper;
import com.meong9.backend.global.utils.RedisKeys;
import com.meong9.backend.global.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlaceWeeklyViewCountJobListener implements JobExecutionListener {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus().isUnsuccessful()) {
            // Job 실패 시 별도의 처리 (필요 시 구현)
            return;
        }

        List<String> categoryNames = CategoryMapper.getAllCategoryNames();

        for (String name : categoryNames) {
            String weeklyKey = RedisKeys.getPlaceWeeklyViewCountKey(name, RedisUtils.formatRelativeToNowDate(1));

            try {
                List<String> dailyKeys = IntStream.range(1, 8)
                        .mapToObj(dayOffset -> RedisKeys.getPlaceDailyViewCountKey(name, RedisUtils.formatRelativeToNowDate(dayOffset)))
                        .toList();
                redisTemplate.opsForZSet().unionAndStore(
                        dailyKeys.get(0),
                        dailyKeys.subList(1, dailyKeys.size()),
                        weeklyKey
                );
            }catch (Exception e){
                log.error("Redis 주간 데이터 집계 중 오류 발생: {}", e.getMessage());
            }

            redisTemplate.expire(weeklyKey, Duration.ofDays(7)); // TTL 7일 설정

        }

        log.info("주간 시설 뷰 카운트 데이터가 성공적으로 생성되었습니다.");
    }
}
