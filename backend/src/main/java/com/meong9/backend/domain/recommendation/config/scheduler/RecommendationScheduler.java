package com.meong9.backend.domain.recommendation.config.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@EnableScheduling
public class RecommendationScheduler {

    private final JobLauncher jobLauncher;

    @Qualifier("recommendationJob")
    private final Job recommendationJob;

//    @Scheduled(cron = "0 0 2 * * ?") // 매일 새벽 2시에 실행
    @Scheduled(cron = "0 */1 * * * ?") // 1분마다
    public void runRecommendationBatch() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
            jobLauncher.run(recommendationJob, jobParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
