package com.meong9.backend.global.batch.place.processor;


import com.meong9.backend.global.batch.place.dto.CreatePlaceFeatureDto;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlaceFeatureProcessor implements ItemProcessor<CreatePlaceFeatureDto, CreatePlaceFeatureDto> {

    @Override
    public CreatePlaceFeatureDto process(CreatePlaceFeatureDto createPlaceFeatureDto) throws Exception {

        if (isValid(createPlaceFeatureDto)) {
            return createPlaceFeatureDto; // Return the processed item
        }
        return null; // Skip invalid items
    }

    private boolean isValid(CreatePlaceFeatureDto createPlaceFeatureDto) {
        return createPlaceFeatureDto.getTopFeatureId() != null && createPlaceFeatureDto.getTopPlaceId() != null;
    }
}
