package com.meong9.backend.domain.recommendation.config;

import com.meong9.backend.domain.address.entity.PlcPenAddress;
import com.meong9.backend.domain.address.repository.PlcPenAddressRepository;
import com.meong9.backend.domain.pension.repository.PensionRepository;
import com.meong9.backend.domain.place.repository.PlaceRepository;
import com.meong9.backend.domain.recommendation.id_class.PensionMemberId;
import com.meong9.backend.domain.recommendation.id_class.PensionPlaceId;
import com.meong9.backend.domain.recommendation.member_score.MemberScoreService;
import com.meong9.backend.domain.recommendation.member_score.pension_place_score.repository.PensionPlaceScoreRepository;
import com.meong9.backend.domain.recommendation.member_score.pension_place_score.service.PensionPlaceScoreService;
import com.meong9.backend.domain.recommendation.recommendation.entity.PensionRecommendation;
import com.meong9.backend.domain.recommendation.recommendation.entity.PlaceRecommendation;
import com.meong9.backend.domain.recommendation.recommendation.projection.PlcPenProjection;
import com.meong9.backend.domain.recommendation.recommendation.service.RecommendationService;
import com.meong9.backend.global.utils.AddressMapper;
import com.meong9.backend.global.utils.RegionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RecommendationBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final RecommendationService recommendationService;
    private final PensionPlaceScoreService pensionPlaceScoreService;
    private final MemberScoreService memberScoreService;
    private final DataSource dataSource;
    private final PensionPlaceScoreRepository pensionPlaceScoreRepository;
    private final PlaceRepository placeRepository;
    private final PensionRepository pensionRepository;
    private final PlcPenAddressRepository plcPenAddressRepository;

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public Job recommendationJob() {
        return new JobBuilder("recommendationJob", jobRepository)
                .start(deleteOldRecommendationsStep())       // 기존 추천 데이터 삭제
                .next(initializeAndUpdateScoresStep())      // 펜션-시설 점수 초기화 및 업데이트
                .next(userBasedStep())                      // 사용자 기반 추천
                .next(itemBasedPlaceStep())                // 펜션 기반 시설 추천
                .next(cleanupScoreStep())                  // 오래된 점수 데이터 삭제
                .incrementer(new RunIdIncrementer())
                .build();
    }


    // 오래된 점수 데이터 정리 Step
    @Bean
    public Step cleanupScoreStep() {
        return new StepBuilder("cleanupScoreStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    memberScoreService.cleanupOldScores(); // 오래된 점수 삭제
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    // 오래된 추천 데이터 정리 Step
    @Bean
    public Step deleteOldRecommendationsStep() {
        return new StepBuilder("deleteOldRecommendationsStep", jobRepository)
                .<Long, Long>chunk(10, transactionManager)
                .reader(deleteOldRecommendationsReader()) // 최근 7일 간 활동한 사용자 조회
                .writer(userRecommendationDeleter()) // 데이터 삭제
                .build();
    }

    // 사용자 기반 추천 Step
    @Bean
    public Step userBasedStep() {
        return new StepBuilder("userBasedStep", jobRepository)
                .listener(new StepExecutionListener() {
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        log.info("user base Step '{}' 시작 - 시간: {}", stepExecution.getStepName(), LocalDateTime.now());
                        stepExecution.getExecutionContext().putLong("startTime", System.currentTimeMillis());
                    }

                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        long startTime = stepExecution.getExecutionContext().getLong("startTime");
                        long endTime = System.currentTimeMillis();
                        log.info("user base Step '{}' 종료 - 시간: {}, 소요 시간: {}ms",
                                stepExecution.getStepName(), LocalDateTime.now(), (endTime - startTime));
                        return stepExecution.getExitStatus();
                    }
                })
                .<Long, List<PensionRecommendation>>chunk(10, transactionManager)
                .reader(userBasedReader())
                .processor(userBasedRecommendationProcessor())
                .writer(userRecommendationWriter())
                .build();
    }


    // 펜션-시설 초기화 및 점수 업데이트 Step
    @Bean
    public Step initializeAndUpdateScoresStep() {
        log.info("초기화 step----");
        return new StepBuilder("initializeAndUpdateScoresStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    pensionPlaceScoreService.initializeAndUpdateScores();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    // 점수 업데이트 및 추천 데이터 생성 Step
    @Bean
    public Step itemBasedPlaceStep() {
        return new StepBuilder("itemBasedPlaceStep", jobRepository)
                .listener(new StepExecutionListener() {
                    @Override
                    public void beforeStep(StepExecution stepExecution) {
                        log.info("item based Step '{}' 시작 - 시간: {}", stepExecution.getStepName(), LocalDateTime.now());
                        stepExecution.getExecutionContext().putLong("startTime", System.currentTimeMillis());
                    }

                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        long startTime = stepExecution.getExecutionContext().getLong("startTime");
                        long endTime = System.currentTimeMillis();
                        log.info("item based Step '{}' 종료 - 시간: {}, 소요 시간: {}ms",
                                stepExecution.getStepName(), LocalDateTime.now(), (endTime - startTime));
                        return stepExecution.getExitStatus();
                    }
                })
                .<Map<String, Object>, List<PlaceRecommendation>>chunk(10, transactionManager)
                .reader(pensionPlaceReader())
                .processor(itemBasedRecommendationProcessor())
                .writer(placeRecommendationWriter())
                .build();
    }


    // 추천 삭제
    @Bean
    public IteratorItemReader<Long> deleteOldRecommendationsReader() {
        List<Long> memberIds = recommendationService.getActiveMemberIds(); // 삭제할 사용자 ID 조회
        log.info("Delete Recommendations Reader initialized with member IDs: {}", memberIds);

        return new IteratorItemReader<>(memberIds.iterator());
    }

    // 사용자 기반 Reader
    @Bean
    public IteratorItemReader<Long> userBasedReader() {
        List<Long> memberIds = recommendationService.getActiveMemberIds();
        List<Long> pensionIds = recommendationService.getPensionsWithReviews();
        log.info("Reader initialized with member IDs: {}", memberIds);
        log.info("Reader initialized with pension IDs: {}", pensionIds);

        // StepExecutionContext에 데이터를 저장
        return new IteratorItemReader<>(memberIds.iterator()) {
            @Override
            public Long read() {
                Long memberId = super.read();
                if (memberId != null) {
                    // ExecutionContext에 pensionIds 저장
                    ExecutionContext executionContext = StepSynchronizationManager.getContext().getStepExecution().getExecutionContext();
                    if (!executionContext.containsKey("pensionIds")) {
                        executionContext.put("pensionIds", pensionIds);
                        log.info("Saved pensionIds to ExecutionContext: {}", pensionIds);
                    }
                }
                log.info("Reader fetched memberId: {}", memberId);
                return memberId;
            }
        };
    }

    // pension_place_score에 있는 펜션 조회 Reader
    @Bean
    public ItemReader<Map<String, Object>> pensionPlaceReader() {
        // 1. 펜션 ID 목록 가져오기
        List<Long> pensionIds = pensionPlaceScoreRepository.findPensionIds();

        // 2. province별 시설 ID 묶기
        Map<String, List<PlcPenProjection>> placeIdsByProvince = getPlaceIdsByProvince();

        // 3. 모든 펜션 ID 가져오기
        List<PlcPenProjection> allPensionInfo = pensionRepository.findPensionProjection();
        List<Long> allPensionIds = new ArrayList<>();
        for (PlcPenProjection projection : allPensionInfo) {
            allPensionIds.add(projection.getId());
        }
        List<PlcPenProjection> pensionProjection = getPensionsWithProvinces(allPensionIds, allPensionInfo);

        // 4. 모든 시설 ID 가져오기
        List<Long> allPlaceIds = placeRepository.findAllPlaceIds();

        // 데이터를 Map으로 묶어서 반환
        Map<String, Object> data = new HashMap<>();
        data.put("pensionIds", pensionIds);
        data.put("placeIdsByProvince", placeIdsByProvince);
        data.put("allPensionIds", allPensionIds);
        data.put("pensionProjection", pensionProjection);
        data.put("allPlaceIds", allPlaceIds);

        return new IteratorItemReader<>(List.of(data).iterator());
    }

    // 사용자 기반 추천 Processor
    @Bean
    public ItemProcessor<Long, List<PensionRecommendation>> userBasedRecommendationProcessor() {
        return userId -> {
            log.info("Processor received userId: {}", userId);

            if (userId == null) {
                log.warn("Processor received null userId.");
                return null;
            }

            // 추천 저장
            // ExecutionContext에서 pensionIds 가져오기
            ExecutionContext executionContext = StepSynchronizationManager.getContext().getStepExecution().getExecutionContext();
            List<Long> pensionIds = (List<Long>) executionContext.get("pensionIds");

            log.info("Processor fetched pensionIds from ExecutionContext: {}", pensionIds);

            // 협업 필터링 결과 가져오기
            List<RecommendedItem> recommendations = recommendationService.recommend(userId, pensionIds, "Pension", null, null);
            log.info("Processor generated recommendations for userId {}: {}", userId, recommendations);

            // 추천 결과를 PensionRecommendation 형태로 변환
            List<PensionRecommendation> pensionRecommendations = new ArrayList<>();
            for (RecommendedItem item : recommendations) {
                PensionRecommendation recommendation = PensionRecommendation.builder()
                        .pensionMemberId(new PensionMemberId(item.getItemID(), userId))
                        .score(item.getValue())
                        .build();
                pensionRecommendations.add(recommendation);
            }

            return pensionRecommendations;
        };
    }


    // item based 추천 데이터 생성 Processor
    @Bean
    public ItemProcessor<Map<String, Object>, List<PlaceRecommendation>> itemBasedRecommendationProcessor() {
        return data -> {
            // Reader에서 전달된 데이터 분리
            List<Long> pensionIds = (List<Long>) data.get("pensionIds");
            Map<String, List<PlcPenProjection>> placeIdsByProvince = (Map<String, List<PlcPenProjection>>) data.get("placeIdsByProvince");
            List<PlcPenProjection> pensionProjection = (List<PlcPenProjection>) data.get("pensionProjection");
            List<Long> allPlaceIds = (List<Long>) data.get("allPlaceIds");

            log.info("Processing pensionIds: {}", pensionIds);
            log.info("Processing allPlaceIds: {}", allPlaceIds);

            // Place IDs by Province 로그 출력
            placeIdsByProvince.forEach((province, projections) ->
                    log.info("Province: {}, Projections: {}",
                            province,
                            projections.stream()
                                    .map(PlcPenProjection::toCustomString)
                                    .collect(Collectors.joining(", "))
                    )
            );

            // Pension Projection 로그 출력
            pensionProjection.forEach(projection ->
                    log.info("PensionProjection: {}", projection.toCustomString())
            );

            List<PlaceRecommendation> recommendations = new ArrayList<>();

            // 추천 생성 로직
            for (PlcPenProjection projection : pensionProjection) {
                Long pensionId = projection.getId();

                log.info("Processing pensionId: {}", pensionId);

                // 추천 호출 전 로그 출력
                log.debug("Calling itemRecommend with parameters - allPlaceIds: {}, pensionId: {}, projection: {}",
                        allPlaceIds,
                        pensionId,
                        projection.toCustomString()
                );

                List<RecommendedItem> results = recommendationService.itemRecommend(
                        allPlaceIds,
                        placeIdsByProvince,
                        pensionId,
                        "Place",
                        projection
                );

                if (results == null || results.isEmpty()) {
                    log.warn("No recommendations found for pensionId: {}", pensionId);
                    continue; // 다음 projection으로 이동
                }

                // 추천 결과 로그
                results.forEach(item ->
                        log.info("Recommended Item - ID: {}, Score: {}", item.getItemID(), item.getValue())
                );

                // PlaceRecommendation 생성
                for (RecommendedItem item : results) {
                    recommendations.add(
                            PlaceRecommendation.builder()
                                    .pensionPlaceId(new PensionPlaceId(pensionId, item.getItemID()))
                                    .score(item.getValue())
                                    .build()
                    );
                }
            }

            return recommendations;
        };
    }




    // 기존 추천 데이터 삭제
    @Bean
    public ItemWriter<Long> userRecommendationDeleter() {
        return memberIds -> {
            try (Connection connection = dataSource.getConnection()) {
                String deleteQuery = """
                DELETE FROM pension_recommendation
                WHERE member_id = ?
            """;

                try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
                    connection.setAutoCommit(false); // 수동 커밋 모드

                    for (Long memberId : memberIds) {
                        preparedStatement.setLong(1, memberId);
                        preparedStatement.addBatch(); // 배치에 추가
                    }

                    preparedStatement.executeBatch(); // 배치 실행
                    connection.commit(); // 트랜잭션 커밋
                } catch (Exception e) {
                    connection.rollback(); // 롤백
                    throw e;
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete recommendations from database", e);
            }
        };
    }


    // 사용자 기반 추천 저장 Writer
    @Bean
    public ItemWriter<List<PensionRecommendation>> userRecommendationWriter() {
        log.info("user base writer 실행중 ---");

        return items -> {
            log.info("Writer received recommendations: {}", items);

            // INSERT, 중복 시 UPDATE
            String insertOrUpdateQuery = """
                INSERT INTO pension_recommendation (member_id, pension_id, score, last_updated_at)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    score = VALUES(score),
                    last_updated_at = NOW()
            """;

            try (Connection connection = dataSource.getConnection()) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(insertOrUpdateQuery)) {
                    connection.setAutoCommit(false); // 수동 커밋 모드

                    for (List<PensionRecommendation> recommendations : items) {
                        for (PensionRecommendation recommendation : recommendations) {
                            preparedStatement.setLong(1, recommendation.getPensionMemberId().getMemberId());
                            preparedStatement.setLong(2, recommendation.getPensionMemberId().getPensionId());
                            preparedStatement.setFloat(3, recommendation.getScore());
                            preparedStatement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                            preparedStatement.addBatch(); // 배치에 추가
                        }
                    }

                    preparedStatement.executeBatch(); // 배치 실행
                    connection.commit(); // 트랜잭션 커밋
                } catch (Exception e) {
                    connection.rollback(); // 롤백
                    throw e;
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to write recommendations to database", e);
            }
        };
    }

    // 펜션 연관 시설 추천 데이터 저장 Writer
    @Bean
    public ItemWriter<List<PlaceRecommendation>> placeRecommendationWriter() {
        return items -> {
            log.info("Writer received recommendations: {}", items);

            // 배치 저장 SQL
            String sql = """
            INSERT INTO place_recommendation (pension_id, place_id, score, last_updated_at)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE score = VALUES(score), last_updated_at = NOW()
        """;

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                connection.setAutoCommit(false);

                for (List<PlaceRecommendation> itemList : items) {
                    for (PlaceRecommendation recommendation : itemList) {
                        Long pensionId = recommendation.getPensionPlaceId().getPensionId();
                        Long placeId = recommendation.getPensionPlaceId().getPlaceId();
                        Float score = recommendation.getScore();

                        // 유효성 검사
                        if (pensionId == null || placeId == null) {
                            log.warn("Skipping invalid recommendation: {}", recommendation);
                            continue;
                        }

                        // SQL 값 설정
                        preparedStatement.setLong(1, pensionId);
                        preparedStatement.setLong(2, placeId);
                        preparedStatement.setFloat(3, score);
                        preparedStatement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                        preparedStatement.addBatch();
                    }
                }

                // 배치 실행
                preparedStatement.executeBatch();
                connection.commit();

            } catch (SQLException e) {
                log.error("Failed to write recommendations. SQLState: {}, ErrorCode: {}, Message: {}",
                        e.getSQLState(), e.getErrorCode(), e.getMessage(), e);
                throw new RuntimeException("Failed to write place recommendations", e);
            }
        };
    }

    public List<PlcPenProjection> getPensionsWithProvinces(List<Long> pensionIds, List<PlcPenProjection> plcPenProjections) {
        // 1. 펜션 ID, 위도, 경도 조회
        List<PlcPenAddress> plcPenAddresses = plcPenAddressRepository.findAddressesByIdsAndType(pensionIds, "020");
        Map<Long, String> addressMap = AddressMapper.mapAddressesProvinceByPlcPenId(plcPenAddresses);

        // 3. 주소 정보를 포함한 PlcPenProjection 생성
        return mapProvincesToPensions(plcPenProjections, addressMap);
    }

    private List<PlcPenProjection> mapProvincesToPensions(
            List<PlcPenProjection> pensionIdList, Map<Long, String> addressMap) {

        List<PlcPenProjection> result = new ArrayList<>();
        for (PlcPenProjection projection : pensionIdList) {
            Long id = projection.getId();
            String province = addressMap.getOrDefault(id, "Unknown");

            PlcPenProjection projectionWithProvince = new PlcPenProjection() {
                @Override
                public Long getId() {
                    return projection.getId();
                }

                @Override
                public Double getLatitude() {
                    return projection.getLatitude();
                }

                @Override
                public Double getLongitude() {
                    return projection.getLongitude();
                }

                @Override
                public String getProvince() {
                    return province;
                }
            };

            result.add(projectionWithProvince);
        }
        return result;
    }

    // 지역(province)별 시설 목록
    private Map<String, List<PlcPenProjection>> getPlaceIdsByProvince() {
        Map<String, List<PlcPenProjection>> placeIdsByProvince = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : RegionMapper.getRegionMapping().entrySet()) {
            String region = entry.getKey();
            List<String> provinces = entry.getValue();

            List<Long> placeIds = new ArrayList<>();

            for (String province : provinces) {
                placeIds.addAll(plcPenAddressRepository.findPlaceIdsByProvince(province));
            }
            List<PlcPenProjection> placeIdList = placeRepository.findPlaceProjectionById(placeIds);
            placeIdsByProvince.put(region, placeIdList);
        }

        return placeIdsByProvince;
    }

}

