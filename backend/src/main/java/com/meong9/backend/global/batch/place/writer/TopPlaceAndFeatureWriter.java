    package com.meong9.backend.global.batch.place.writer;

    import com.meong9.backend.domain.place.entity.TopPlace;
    import com.meong9.backend.global.batch.place.dto.TopPlaceAndFeature;
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
    public class TopPlaceAndFeatureWriter implements ItemWriter<TopPlaceAndFeature> {

        private final JdbcTemplate jdbcTemplate;

        @Override
        public void write(Chunk<? extends TopPlaceAndFeature> items) throws Exception {
            // 1. TopPlace 저장 및 ID 반환
            List<Long> placeIds = insertTopPlaces(items);

            // 2. TopFeature 저장 및 ID 반환
            List<Long> featureIds = insertTopFeatures(items);

            // 3. IDs를 ExecutionContext에 저장
            appendToExecutionContext("topPlaceIds", placeIds);
            appendToExecutionContext("topFeatureIds", featureIds);
        }

        private List<Long> insertTopPlaces(Chunk<? extends TopPlaceAndFeature> items) {
            List<Long> generatedIds = new ArrayList<>();
            String sql =
                    "INSERT INTO top_place (place_name, review_count, review_avg, like_count, " +
                            "province, city_district, sub_district, view_count, `rank`, year, month, `date`, place_id, category) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            for (TopPlaceAndFeature item : items.getItems()) {
                TopPlace topPlace = item.getTopPlace();
                // GeneratedKeyHolder 사용
                KeyHolder keyHolder = new GeneratedKeyHolder();
                jdbcTemplate.update(connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[]{"top_place_id"});
                    ps.setString(1, topPlace.getPlaceName());
                    ps.setInt(2, topPlace.getReviewCount());
                    ps.setBigDecimal(3, topPlace.getReviewAvg());
                    ps.setInt(4, topPlace.getLikeCount());
                    ps.setString(5, topPlace.getProvince());
                    ps.setString(6, topPlace.getCityDistrict());
                    ps.setString(7, topPlace.getSubDistrict());
                    ps.setLong(8, topPlace.getViewCount());
                    ps.setInt(9, topPlace.getRank());
                    ps.setInt(10, topPlace.getYear());
                    ps.setInt(11, topPlace.getMonth());
                    ps.setInt(12, topPlace.getDate());
                    ps.setLong(13, topPlace.getPlaceId());
                    ps.setString(14, topPlace.getCategory());
                    return ps;
                }, keyHolder);
                // 생성된 ID 저장
                generatedIds.add(keyHolder.getKey().longValue());
            }
            return generatedIds;
        }

        private List<Long> insertTopFeatures(Chunk<? extends TopPlaceAndFeature> items) {
            List<Long> generatedIds = new ArrayList<>();
            String sql =
                    "INSERT INTO top_feature (parking, pet_only_area, indoor_space, " +
                            "outdoor_space, weight_limit, swimming_pool, barbecue, bulmeong, fence, barking, no_smoking) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            for (TopPlaceAndFeature item : items.getItems()) {
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
            List<Long> existingIds;// 가변 리스트로 변환
            if (jobContext.containsKey(key)) {
                existingIds = new ArrayList<>((List<Long>) jobContext.get(key));
                log.info("ExecutionContext에 기존 Key '{}'가 있습니다. 기존 데이터: {}", key, existingIds);
            }
            else {
                existingIds = new ArrayList<>();
                log.info("ExecutionContext에 Key '{}'가 없습니다. 새 리스트로 초기화합니다.", key);

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
