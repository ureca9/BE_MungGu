    package com.meong9.backend.global.batch.pension.writer;

    import com.meong9.backend.domain.pension.entity.TopPension;
    import com.meong9.backend.global.batch.pension.dto.TopPensionAndFeature;
    import com.meong9.backend.global.topFeature.entity.TopFeature;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.batch.core.JobExecution;
    import org.springframework.batch.core.StepExecution;
    import org.springframework.batch.core.scope.context.StepSynchronizationManager;
    import org.springframework.batch.item.Chunk;
    import org.springframework.batch.item.ExecutionContext;
    import org.springframework.batch.item.ItemWriter;
    import org.springframework.jdbc.core.JdbcTemplate;
    import org.springframework.jdbc.support.GeneratedKeyHolder;
    import org.springframework.jdbc.support.KeyHolder;
    import org.springframework.stereotype.Component;

    import java.sql.PreparedStatement;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Objects;

    @Component
    @RequiredArgsConstructor
    @Slf4j
    public class TopPensionAndFeatureWriter implements ItemWriter<TopPensionAndFeature> {

        private final JdbcTemplate jdbcTemplate;

        @Override
        public void write(Chunk<? extends TopPensionAndFeature> items) throws Exception {
            // 1. TopPension 저장 및 ID 반환
            List<Long> pensionIds = insertTopPensions(items);

            // 2. TopFeature 저장 및 ID 반환
            List<Long> featureIds = insertTopFeatures(items);

            // 3. IDs를 ExecutionContext에 저장
            appendToExecutionContext("topPensionIds", pensionIds);
            appendToExecutionContext("topFeatureIds", featureIds);
        }

        private List<Long> insertTopPensions(Chunk<? extends TopPensionAndFeature> items) {
            List<Long> generatedIds = new ArrayList<>();
            String sql =
                    "INSERT INTO top_pension (pension_name, review_count, review_avg, like_count, " +
                            "province, city_district, sub_district, view_count, `rank`, year, month, `date`, room_price_avg, pension_id) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            for (TopPensionAndFeature item : items.getItems()) {
                TopPension topPension = item.getTopPension();
                // GeneratedKeyHolder 사용
                KeyHolder keyHolder = new GeneratedKeyHolder();
                jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[]{"top_pension_id"});
                    ps.setString(1, topPension.getPensionName());
                    ps.setInt(2, topPension.getReviewCount());
                    ps.setBigDecimal(3, topPension.getReviewAvg());
                    ps.setInt(4, topPension.getLikeCount());
                    ps.setString(5, topPension.getProvince());
                    ps.setString(6, topPension.getCityDistrict());
                    ps.setString(7, topPension.getSubDistrict());
                    ps.setLong(8, topPension.getViewCount());
                    ps.setInt(9, topPension.getRank());
                    ps.setInt(10, topPension.getYear());
                    ps.setInt(11, topPension.getMonth());
                    ps.setInt(12, topPension.getDate());
                    ps.setBigDecimal(13, topPension.getRoomPriceAvg());
                    ps.setLong(14, topPension.getPensionId());
                    return ps;
                }, keyHolder);
                // 생성된 ID 저장
                generatedIds.add(keyHolder.getKey().longValue());
            }
            return generatedIds;
        }

        private List<Long> insertTopFeatures(Chunk<? extends TopPensionAndFeature> items) {
            List<Long> generatedIds = new ArrayList<>();
            String sql =
                    "INSERT INTO top_feature (parking, pet_only_area, indoor_space, " +
                            "outdoor_space, weight_limit, swimming_pool, barbecue, bulmeong, fence, barking, no_smoking) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            for (TopPensionAndFeature item : items.getItems()) {
                TopFeature topFeature = item.getTopFeature();
                // GeneratedKeyHolder 사용
                KeyHolder keyHolder = new GeneratedKeyHolder();
                jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[]{"top_feature_id"});
                    ps.setInt(1, topFeature.getParking() ? 1 : 0);
                    ps.setInt(2, topFeature.getPetOnlyArea() ? 1 : 0);
                    ps.setInt(3, topFeature.getIndoorSpace() ? 1 : 0);
                    ps.setInt(4, topFeature.getOutdoorSpace() ? 1 : 0);
                    ps.setInt(5, topFeature.getWeightLimit() ? 1 : 0);
                    ps.setInt(6, topFeature.getSwimmingPool() ? 1 : 0);
                    ps.setInt(7, topFeature.getBarbecue() ? 1 : 0);
                    ps.setInt(8, topFeature.getBulmeong() ? 1 : 0);
                    ps.setInt(9, topFeature.getFence() ? 1 : 0);
                    ps.setInt(10, topFeature.getBarking() ? 1 : 0);
                    ps.setInt(11, topFeature.getNoSmoking() ? 1 : 0);
                    return ps;
                }, keyHolder);
                generatedIds.add(keyHolder.getKey().longValue());
            }
            return generatedIds;
        }


        private void appendToExecutionContext(String key, List<Long> ids) {
            // StepExecution 가져오기
            StepExecution stepExecution = Objects.requireNonNull(StepSynchronizationManager.getContext()).getStepExecution();

            // JobExecution 가져오기
            JobExecution jobExecution = stepExecution.getJobExecution();

            // 검증 로직: ID 리스트가 비어 있는지 확인
            if (ids == null || ids.isEmpty()) {
                log.warn("ExecutionContext에 저장하려는 ID 리스트가 비어 있습니다. Key: {}", key);
                return;
            }

            // JobExecution의 ExecutionContext 가져오기
            ExecutionContext jobContext = jobExecution.getExecutionContext();

            // 기존 데이터 가져오기 또는 새 리스트 생성
            @SuppressWarnings("unchecked")
            List<Long> existingIds = (List<Long>) jobContext.get(key);
            if (existingIds == null) {
                log.info("ExecutionContext에 Key '{}'가 없습니다. 새 리스트로 초기화합니다.", key);
                existingIds = new ArrayList<>();
            } else {
                log.info("ExecutionContext에 기존 Key '{}'가 있습니다. 기존 데이터: {}", key, existingIds);
            }

            // 기존 데이터와 새 데이터 병합
            existingIds.addAll(ids);

            // 중복 제거
            List<Long> uniqueIds = existingIds.stream().distinct().toList();

            // JobExecution의 ExecutionContext에 저장
            jobContext.put(key, uniqueIds);

            // 로그 출력: 누적 데이터 확인
            log.info("ExecutionContext에 누적된 총 데이터. Key: {}, 누적된 IDs: {}", key, jobContext.get(key, List.class));
            log.debug("현재 ExecutionContext 상태: {}", jobContext);
        }


    }
