package com.meong9.backend.global.batch.pension.writer;



import com.meong9.backend.domain.pension.entity.TopPension;
import com.meong9.backend.domain.pension.repository.TopPensionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TopPensionWriter implements ItemWriter<TopPension> {

    private final TopPensionRepository topPensionRepository;

    @Override
    public void write(Chunk<? extends TopPension> items) {
        // 배치 인서트
        topPensionRepository.saveAll(items);
    }
}

