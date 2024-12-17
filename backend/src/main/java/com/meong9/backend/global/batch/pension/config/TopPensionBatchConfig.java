package com.meong9.backend.global.batch.pension.config;

import com.meong9.backend.domain.pension.entity.TopPension;
import com.meong9.backend.global.batch.pension.dto.RedisTopPensionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class TopPensionBatchConfig {

    private JobLauncher jobLauncher;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job aggregateTopPensionJob(Step aggregateTopPensionStep) {
        return new JobBuilder("aggregateTopPensionJob", jobRepository)
                .start(aggregateTopPensionStep)
                .preventRestart() // 재시작 가능 설정
                .build();
    }

    @Bean
    public Step aggregateTopPensionStep(ItemReader<RedisTopPensionDto> topPensionReader,
                                        ItemProcessor<RedisTopPensionDto, TopPension> topPensionProcessor,
                                        ItemWriter<TopPension> topPensionWriter) {
        return new StepBuilder("aggregateTopPensionStep", jobRepository)
                .<RedisTopPensionDto, TopPension>chunk(100, transactionManager) // 타입 수정
                .reader(topPensionReader)
                .processor(topPensionProcessor)
                .writer(topPensionWriter)
                .allowStartIfComplete(true) // 이미 완료된 Step도 재시작 가능
                .build();
    }
}
