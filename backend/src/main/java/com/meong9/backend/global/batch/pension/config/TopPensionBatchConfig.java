package com.meong9.backend.global.batch.pension.config;

import com.meong9.backend.global.batch.pension.dto.CreatePensionFeatureDto;
import com.meong9.backend.global.batch.pension.dto.RedisTopPensionDto;
import com.meong9.backend.global.batch.pension.dto.TopPensionAndFeature;
import com.meong9.backend.global.batch.pension.listener.PensionJobExecutionContextCleaner;
import com.meong9.backend.global.batch.pension.listener.StepListener;
import com.meong9.backend.global.batch.pension.listener.TopPensionJobListener;
import com.meong9.backend.global.batch.pension.processor.PensionFeatureProcessor;
import com.meong9.backend.global.batch.pension.processor.TopPensionAndFeatureProcessor;
import com.meong9.backend.global.batch.pension.reader.PensionFeatureReader;
import com.meong9.backend.global.batch.pension.reader.TopPensionRedisReader;
import com.meong9.backend.global.batch.pension.writer.PensionFeatureWriter;
import com.meong9.backend.global.batch.pension.writer.TopPensionAndFeatureWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
@Slf4j
public class TopPensionBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TopPensionJobListener topPensionJobListener;
    private final PensionJobExecutionContextCleaner pensionJobExecutionContextCleaner;

    @Bean
    public Job aggregateTopPensionJob(
            Step saveTopPensionAndFeatureStep,
            Step savePensionFeatureStep) {
        return new JobBuilder("aggregateTopPensionJob", jobRepository)
                .listener(topPensionJobListener)
                .listener(pensionJobExecutionContextCleaner)
                .start(saveTopPensionAndFeatureStep)
                .on("FAILED").end() // Step 실패 시 Job 종료
                .from(saveTopPensionAndFeatureStep)
                .on("COMPLETED").to(savePensionFeatureStep)
                .from(savePensionFeatureStep)
                .on("FAILED").fail() // Step 실패 시 Job 실패
                .from(savePensionFeatureStep)
                .on("COMPLETED").end()
                .end()
                .build();
    }

    @Bean
    public Step saveTopPensionAndFeatureStep(TopPensionRedisReader reader,
                                             TopPensionAndFeatureProcessor processor,
                                             TopPensionAndFeatureWriter writer) {
        return new StepBuilder("saveTopPensionAndFeatureStep", jobRepository)
                .<RedisTopPensionDto, TopPensionAndFeature>chunk(100, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener(new StepListener()) // StepListener 추가
                .build();
    }

    @Bean
    public Step savePensionFeatureStep(PensionFeatureReader reader,
                                       PensionFeatureProcessor processor,
                                       PensionFeatureWriter writer) {
        return new StepBuilder("savePensionFeatureStep", jobRepository)
                .<CreatePensionFeatureDto, CreatePensionFeatureDto>chunk(100, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener(new StepListener()) // StepListener 추가
                .build();
    }
}
