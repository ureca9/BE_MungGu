package com.meong9.backend.global.batch.place.config;

import com.meong9.backend.global.batch.place.dto.CreatePlaceFeatureDto;
import com.meong9.backend.global.batch.place.dto.RedisTopPlaceDto;
import com.meong9.backend.global.batch.place.dto.TopPlaceAndFeature;
import com.meong9.backend.global.batch.place.listener.PlaceJobExecutionContextCleaner;
import com.meong9.backend.global.batch.pension.listener.StepListener;
import com.meong9.backend.global.batch.place.listener.PlaceWeeklyViewCountJobListener;
import com.meong9.backend.global.batch.place.processor.PlaceFeatureProcessor;
import com.meong9.backend.global.batch.place.processor.TopPlaceAndFeatureProcessor;
import com.meong9.backend.global.batch.place.reader.PlaceFeatureReader;
import com.meong9.backend.global.batch.place.reader.TopPlaceRedisReader;
import com.meong9.backend.global.batch.place.writer.PlaceFeatureWriter;
import com.meong9.backend.global.batch.place.writer.TopPlaceAndFeatureWriter;
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
public class TopPlaceBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final PlaceJobExecutionContextCleaner placeJobExecutionContextCleaner;
    private final PlaceWeeklyViewCountJobListener placWeeklyViewCountJobListener;

    @Bean
    public Job aggregateTopPlaceJob(
            Step saveTopPlaceAndFeatureStep,
            Step savePlaceFeatureStep) {
        return new JobBuilder("aggregateTopPlaceJob", jobRepository)
                .listener(placeJobExecutionContextCleaner)
                .listener(placWeeklyViewCountJobListener)
                .start(saveTopPlaceAndFeatureStep)
                .on("FAILED").end() // Step 실패 시 Job 종료
                .from(saveTopPlaceAndFeatureStep)
                .on("COMPLETED").to(savePlaceFeatureStep)
                .from(savePlaceFeatureStep)
                .on("FAILED").fail() // Step 실패 시 Job 실패
                .from(savePlaceFeatureStep)
                .on("COMPLETED").end()
                .end()
                .build();
    }

    @Bean
    public Step saveTopPlaceAndFeatureStep(TopPlaceRedisReader reader,
                                           TopPlaceAndFeatureProcessor processor,
                                           TopPlaceAndFeatureWriter writer) {
        return new StepBuilder("saveTopPlaceAndFeatureStep", jobRepository)
                .<RedisTopPlaceDto, TopPlaceAndFeature>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener(new StepListener()) // StepListener 추가
                .build();
    }

    @Bean
    public Step savePlaceFeatureStep(PlaceFeatureReader reader,
                                     PlaceFeatureProcessor processor,
                                     PlaceFeatureWriter writer) {
        return new StepBuilder("savePlaceFeatureStep", jobRepository)
                .<CreatePlaceFeatureDto, CreatePlaceFeatureDto>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener(new StepListener()) // StepListener 추가
                .build();
    }
}
