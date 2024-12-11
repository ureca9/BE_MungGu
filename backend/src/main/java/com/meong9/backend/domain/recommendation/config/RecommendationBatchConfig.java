package com.meong9.backend.domain.recommendation.config;

import com.meong9.backend.domain.recommendation.id_class.PensionMemberId;
import com.meong9.backend.domain.recommendation.member_score.MemberScoreService;
import com.meong9.backend.domain.recommendation.member_score.pension_place_score.service.PensionPlaceScoreService;
import com.meong9.backend.domain.recommendation.recommendation.entity.PensionRecommendation;
import com.meong9.backend.domain.recommendation.recommendation.entity.PlaceRecommendation;
import com.meong9.backend.domain.recommendation.recommendation.service.RecommendationService;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
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
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RecommendationBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final RecommendationService recommendationService;
    private final EntityManagerFactory entityManagerFactory;
    private final PensionPlaceScoreService pensionPlaceScoreService;
    private final MemberScoreService memberScoreService;
    private final DataSource dataSource;


    @Bean
    public Job recommendationJob() {
        return new JobBuilder("recommendationJob", jobRepository)
                .start(userBasedStep())                // 사용자 기반 추천
                .next(itemBasedPlaceStep())          // 펜션 기반 시설 추천 (+ 점수 업데이트)
                .next(cleanupScoreStep()) // 오래된 데이터 삭제
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

    // 사용자 기반 추천 Step
    @Bean
    public Step userBasedStep() {
        return new StepBuilder("userBasedStep", jobRepository)
                .<Long, List<PensionRecommendation>>chunk(10, transactionManager)
                .reader(userBasedReader()) // Reader 호출
                .processor(userRecommendationProcessor()) // Processor 호출
                .writer(userRecommendationWriter()) // Writer 호출
                .build();
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
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
        log.info("item base step 시작");
        return new StepBuilder("itemBasedPlaceStep", jobRepository)
                .<Long, List<PlaceRecommendation>>chunk(10, transactionManager)
                .reader(pensionItemReader())           // 리뷰가 1개 이상 있는 펜션 조회
                .processor(combinedProcessor())        // 점수 업데이트와 추천 데이터 생성
                .writer(placeRecommendationWriter())   // 리스트를 저장하는 Writer
                .build();
    }



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





    // 리뷰가 1개 이상 있는 펜션 조회 Reader
    @Bean
    public IteratorItemReader<Long> pensionItemReader() {
        List<Long> pensionIds = recommendationService.getPensionsWithReviews();
        return new IteratorItemReader<>(pensionIds.iterator());
    }

    // 사용자 기반 추천 Processor
    @Bean
    public ItemProcessor<Long, List<PensionRecommendation>> userRecommendationProcessor() {
        return userId -> {
            log.info("Processor received userId: {}", userId);

            if (userId == null) {
                log.warn("Processor received null userId.");
                return null;
            }

            // ExecutionContext에서 pensionIds 가져오기
            ExecutionContext executionContext = StepSynchronizationManager.getContext().getStepExecution().getExecutionContext();
            List<Long> pensionIds = (List<Long>) executionContext.get("pensionIds");

            log.info("Processor fetched pensionIds from ExecutionContext: {}", pensionIds);

            // 협업 필터링 결과 가져오기
            List<RecommendedItem> recommendations = recommendationService.recommend(userId, pensionIds, "Pension");
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



    // 점수 업데이트 + 추천 데이터 생성 Processor
    @Bean
    public ItemProcessor<Long, List<PlaceRecommendation>> combinedProcessor() {
        return pensionId -> {
            log.info("Processing item-based recommendation for pensionId: {}", pensionId);
            return pensionPlaceScoreService.recommendPlacesForPension(pensionId);
        };
    }


    // 사용자 기반 추천 저장 Writer
    @Bean
    public ItemWriter<List<PensionRecommendation>> userRecommendationWriter() {
        log.info("user base writer 실행중 ---");

        return items -> {
            try (Connection connection = dataSource.getConnection()) {
                // INSERT, 중복 시 UPDATE
                String insertOrUpdateQuery = """
                INSERT INTO pension_recommendation (member_id, pension_id, score, last_updated_at)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    score = VALUES(score),
                    last_updated_at = NOW()
            """;

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
        log.info("펜션-시설 저장----------");

        return items -> {
            // INSERT 또는 중복 시 UPDATE를 수행하는 SQL
            String sql = """
            INSERT INTO place_recommendation (pension_id, place_id, score, last_updated_at)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                score = VALUES(score),
                last_updated_at = NOW()
        """;

            try (Connection connection = dataSource.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                connection.setAutoCommit(false); // 수동 커밋 모드

                for (List<PlaceRecommendation> itemList : items) {
                    for (PlaceRecommendation recommendation : itemList) {
                        preparedStatement.setLong(1, recommendation.getPensionPlaceId().getPensionId());
                        preparedStatement.setLong(2, recommendation.getPensionPlaceId().getPlaceId());
                        preparedStatement.setFloat(3, recommendation.getScore());
                        preparedStatement.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                        preparedStatement.addBatch(); // 배치에 추가
                    }
                }

                preparedStatement.executeBatch(); // 배치 실행
                connection.commit(); // 트랜잭션 커밋
            } catch (SQLException e) {
                throw new RuntimeException("Failed to write place recommendations to database", e);
            }
        };
    }


}

