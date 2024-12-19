package com.meong9.backend.global.batch.place.writer;

import com.meong9.backend.global.batch.place.dto.CreatePlaceFeatureDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PlaceFeatureWriter implements ItemWriter<CreatePlaceFeatureDto> {

    private final JdbcBatchItemWriter<CreatePlaceFeatureDto> placeFeatureWriter;

    @Override
    public void write(Chunk<? extends CreatePlaceFeatureDto> items) throws Exception {
        log.info("삽입 데이터 목록: {}",
                items.getItems().stream() // items를 getItems()로 변환
                        .map(item -> String.format("topPlaceId: %d, topFeatureId: %d",
                                item.getTopPlaceId(),
                                item.getTopFeatureId()))
                        .toList());

        placeFeatureWriter.write(items); // 실제 데이터를 넘길 때 getItems() 사용
    }
}

