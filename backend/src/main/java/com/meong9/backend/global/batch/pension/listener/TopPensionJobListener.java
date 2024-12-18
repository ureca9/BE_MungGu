package com.meong9.backend.global.batch.pension.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TopPensionJobListener implements JobExecutionListener {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Job이 성공적으로 종료되면 Redis의 조회수 데이터를 삭제합니다.
     */
    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus().isUnsuccessful()) {
            log.warn("Job 실패로 Redis 데이터 삭제 생략");
            return;
        }

        // Redis 데이터 삭제
        clearAllPensionViewCounts();
        log.info("Redis의 펜션 조회수 데이터가 삭제되었습니다.");
    }

    /**
     * 모든 펜션의 조회수 데이터를 삭제합니다.
     */
    private void clearAllPensionViewCounts() {
        String sortedSetKey = "pension:viewCount";
        redisTemplate.delete(sortedSetKey);
    }
}
