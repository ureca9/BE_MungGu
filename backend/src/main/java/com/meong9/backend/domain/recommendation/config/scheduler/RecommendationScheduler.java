package com.meong9.backend.domain.recommendation.config.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@EnableScheduling
@Slf4j
public class RecommendationScheduler {

    private final JobLauncher jobLauncher;

    @Qualifier("recommendationJob")
    private final Job recommendationJob;

    @Scheduled(cron = "0 0 2 * * ?") // 매일 새벽 2시에 실행
//    @Scheduled(cron = "0 */1 * * * ?") // 1분마다
    public void runRecommendationBatch() {
        log.info("추천 배치 작업 시작 - 시작 시간: {}", LocalDateTime.now());
        long startTime = System.currentTimeMillis();
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
            jobLauncher.run(recommendationJob, jobParameters);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            long endTime = System.currentTimeMillis(); // 종료 시간 기록
            log.info("추천 배치 작업 종료 - 종료 시간: {}, 소요 시간: {}ms",
                    LocalDateTime.now(), (endTime - startTime));
        }
    }
}
