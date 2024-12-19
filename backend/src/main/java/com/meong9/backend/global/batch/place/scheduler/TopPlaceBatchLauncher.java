package com.meong9.backend.global.batch.place.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class TopPlaceBatchLauncher {

    private final JobLauncher jobLauncher;

    private final Job aggregateTopPlaceJob;

    @Retryable(
            value = { JobExecutionException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 5000)
    )
    @Scheduled(cron = "0 23 14 * * ?")
    public void launchTopPlaceJob() {
        long startTime = System.nanoTime();
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("jobId", UUID.randomUUID().toString())
                    .addString("executionDate", LocalDate.now().toString())
                    .addLong("time", System.currentTimeMillis()) // 간결한 키
                    .toJobParameters();
            log.info("Top Pension Job 시작 - 배치ID: {}, 시작 시간: {}",
                    jobParameters.getString("jobId"), LocalDateTime.now());
            jobLauncher.run(aggregateTopPlaceJob, jobParameters);
        } catch (JobExecutionException e) {
            log.error("Top_Place 배치 작업 실행 중 오류 발생: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Top_Place 예상치 못한 오류 발생: {}", e.getMessage(), e);
        } finally {
            long endTime = System.currentTimeMillis();
            log.info("Top Place Job 종료 - 종료 시간: {}, 소요 시간: {}ms",
                    LocalDateTime.now(), TimeUnit.NANOSECONDS.toMillis(endTime - startTime));
        }
    }
}