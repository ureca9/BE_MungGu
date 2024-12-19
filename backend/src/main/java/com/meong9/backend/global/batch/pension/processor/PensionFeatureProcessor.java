package com.meong9.backend.global.batch.pension.processor;


import com.meong9.backend.global.batch.pension.dto.CreatePensionFeatureDto;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PensionFeatureProcessor implements ItemProcessor<CreatePensionFeatureDto, CreatePensionFeatureDto> {

    @Override
    public CreatePensionFeatureDto process(CreatePensionFeatureDto createPensionFeatureDto) throws Exception {

        if (isValid(createPensionFeatureDto)) {
            return createPensionFeatureDto; // Return the processed item
        }
        return null; // Skip invalid items
    }

    private boolean isValid(CreatePensionFeatureDto createPensionFeatureDto) {
        return createPensionFeatureDto.getTopFeatureId() != null && createPensionFeatureDto.getTopPensionId() != null;
    }
}
