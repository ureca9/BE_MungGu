package com.meong9.backend.global.batch.place.processor;


import com.meong9.backend.global.batch.place.dto.CreatePlaceFeatureDto;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlaceFeatureProcessor implements ItemProcessor<CreatePlaceFeatureDto, CreatePlaceFeatureDto> {

    private static final Logger log = LoggerFactory.getLogger(PlaceFeatureProcessor.class);

    @Override
    public CreatePlaceFeatureDto process(CreatePlaceFeatureDto createPlaceFeatureDto) throws Exception {
        try {
            log.debug("Processing PlaceFeature: {}", createPlaceFeatureDto);

            if (isValid(createPlaceFeatureDto)) {
                log.debug("Valid PlaceFeature processed: {}", createPlaceFeatureDto);
                return createPlaceFeatureDto;
            }
            log.warn("Invalid PlaceFeature skipped: {}", createPlaceFeatureDto);
            return null;
        } catch (Exception e) {
            log.error("Error processing PlaceFeature: {}", createPlaceFeatureDto, e);
            throw new IllegalAccessException("PlaceFeature 처리 중 오류 발생");
        }
    }

    private boolean isValid(CreatePlaceFeatureDto createPlaceFeatureDto) {
        return createPlaceFeatureDto.getTopFeatureId() != null && createPlaceFeatureDto.getTopPlaceId() != null;
    }
}
