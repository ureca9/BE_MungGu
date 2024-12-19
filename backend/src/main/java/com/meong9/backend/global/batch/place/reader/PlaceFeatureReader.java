package com.meong9.backend.global.batch.place.reader;

import com.meong9.backend.global.batch.place.dto.CreatePlaceFeatureDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Component
@Slf4j
public class PlaceFeatureReader implements ItemReader<CreatePlaceFeatureDto> {

    private List<Long> topPlaceIds; // topPlaceIds를 필드로 선언
    private List<Long> topFeatureIds; // topFeatureIds를 필드로 선언
    private Iterator<CreatePlaceFeatureDto> iterator;
    private int currentIndex = 0; // 인덱스 추적용 변수

    @BeforeStep
    public void initialize(StepExecution stepExecution) {
        ExecutionContext jobContext = stepExecution.getJobExecution().getExecutionContext();

        // 로그 추가
        log.debug("ExecutionContext 상태: {}", jobContext);

        this.topPlaceIds = getListFromContext(jobContext, "topPlaceIds");
        this.topFeatureIds = getListFromContext(jobContext, "topFeatureIds");

        // 데이터 검증
        if (topPlaceIds == null || topPlaceIds.isEmpty() || topFeatureIds.isEmpty()) {
            // JobExecution 상태를 FAILED로 설정
            stepExecution.setExitStatus(ExitStatus.FAILED);
            stepExecution.getJobExecution().setExitStatus(ExitStatus.FAILED);
            throw new IllegalStateException("ExecutionContext에 데이터가 없으므로 Job을 종료합니다.");
        }
        log.info("PlaceFeatureReader 초기화 완료. topPlaceIds: {}, topFeatureIds: {}", topPlaceIds, topFeatureIds);

        // CreatePlaceFeatureDto 리스트 생성
        List<CreatePlaceFeatureDto> features = new ArrayList<>();
        for (int i = 0; i < topPlaceIds.size(); i++) {
            features.add(new CreatePlaceFeatureDto(topPlaceIds.get(i), topFeatureIds.get(i)));
        }

        this.iterator = features.iterator();
        this.currentIndex = 0; // 초기화 시 인덱스도 초기화
    }

    @SuppressWarnings("unchecked")
    private List<Long> getListFromContext(ExecutionContext context, String key) {
        Object value = context.get(key);
        if (value instanceof List<?>) {
            // 리스트 내부 요소의 타입 확인
            List<?> list = (List<?>) value;
            if (!list.isEmpty() && list.get(0) instanceof Long) {
                return (List<Long>) list;
            }
        }
        throw new IllegalArgumentException("ExecutionContext의 " + key + " 값이 올바르지 않습니다.");
    }

    @Override
    public CreatePlaceFeatureDto read() {
        if (iterator != null && iterator.hasNext()) {
            CreatePlaceFeatureDto nextFeature = iterator.next();

            // 현재 인덱스를 추적하고 ExecutionContext에 저장
            currentIndex++;
            StepSynchronizationManager.getContext()
                    .getStepExecution()
                    .getExecutionContext()
                    .putInt("currentIndex", currentIndex);

            return nextFeature;
        }
        return null; // 더 이상 읽을 항목이 없을 때
    }
}
