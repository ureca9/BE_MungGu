package com.meong9.backend.global.batch.pension.writer;

import com.meong9.backend.global.batch.pension.dto.CreatePensionFeatureDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PensionFeatureWriter implements ItemWriter<CreatePensionFeatureDto> {

    private final JdbcBatchItemWriter<CreatePensionFeatureDto> pensionFeatureWriter;

    @Override
    public void write(Chunk<? extends CreatePensionFeatureDto> items) throws Exception {
        log.info("삽입 데이터 목록: {}",
                items.getItems().stream() // items를 getItems()로 변환
                        .map(item -> String.format("topPensionId: %d, topFeatureId: %d",
                                item.getTopPensionId(),
                                item.getTopFeatureId()))
                        .toList());

        pensionFeatureWriter.write(items); // 실제 데이터를 넘길 때 getItems() 사용
    }
}

