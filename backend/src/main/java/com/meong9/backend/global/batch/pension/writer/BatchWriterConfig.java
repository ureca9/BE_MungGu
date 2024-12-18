package com.meong9.backend.global.batch.pension.writer;

import com.meong9.backend.domain.pension.entity.PensionFeature;
import com.meong9.backend.domain.pension.entity.TopPension;
import com.meong9.backend.global.batch.pension.dto.CreatePensionFeatureDto;
import com.meong9.backend.global.topFeature.entity.TopFeature;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class BatchWriterConfig {

    private final DataSource dataSource;

    @Bean
    public JdbcBatchItemWriter<CreatePensionFeatureDto> pensionAndFeatureWriter() {
        return new JdbcBatchItemWriterBuilder<CreatePensionFeatureDto>()
                .dataSource(dataSource)
                .sql("INSERT INTO pension_feature (top_pension_id, top_feature_id) VALUES (:topPensionId, :topFeatureId)")
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .build();
    }


}
