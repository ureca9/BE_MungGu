package com.meong9.backend.global.batch.place.writer;

import com.meong9.backend.global.batch.place.dto.CreatePlaceFeatureDto;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class PlaceFeatureBatchWriterConfig {

    private final DataSource dataSource;

    @Bean
    public JdbcBatchItemWriter<CreatePlaceFeatureDto> placeAndFeatureWriter() {
        return new JdbcBatchItemWriterBuilder<CreatePlaceFeatureDto>()
                .dataSource(dataSource)
                .sql("INSERT INTO place_feature (top_place_id, top_feature_id) VALUES (:topPlaceId, :topFeatureId)")
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .build();
    }


}
