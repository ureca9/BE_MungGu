package com.meong9.backend.domain.recommendation.config;

import com.meong9.backend.domain.recommendation.member_score.MemberScoreService;
import com.meong9.backend.domain.recommendation.id_class.PensionMemberId;
import com.meong9.backend.domain.recommendation.member_score.pension_place_score.service.PensionPlaceScoreService;
import com.meong9.backend.domain.recommendation.recommendation.entity.PensionRecommendation;
import com.meong9.backend.domain.recommendation.recommendation.entity.PlaceRecommendation;
import com.meong9.backend.domain.recommendation.recommendation.service.RecommendationService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.support.IteratorItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;
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


    @Bean
    public Job recommendationJob() {
        return new JobBuilder("recommendationJob", jobRepository)
                .start(userBasedStep())                // 사용자 기반 추천
                .next(initializeAndUpdateScoresStep()) // 펜션-시설 점수 초기화 및 업데이트
                .next(reviewBasedPlaceStep())          // 펜션 기반 시설 추천
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




    // 펜션-시설 초기화 및 점수 업데이트 Step
    @Bean
    public Step initializeAndUpdateScoresStep() {
        return new StepBuilder("initializeAndUpdateScoresStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    pensionPlaceScoreService.initializeAndUpdateScores();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    // 점수 업데이트 및 추천 데이터 생성 Step
    @Bean
    public Step reviewBasedPlaceStep() {

        return new StepBuilder("reviewBasedPlaceStep", jobRepository)
                .<Long, List<PlaceRecommendation>>chunk(10, transactionManager)
                .reader(pensionItemReader())           // 리뷰가 1개 이상 있는 펜션 조회
                .processor(combinedProcessor())        // 점수 업데이트와 추천 데이터 생성
                .writer(placeRecommendationWriter())   // 추천 데이터 저장
                .build();
    }

    @Bean
    public IteratorItemReader<Long> userBasedReader() {
        List<Long> memberIds = recommendationService.getActiveMemberIds();
        log.info("Reader initialized with member IDs: {}", memberIds);
        return new IteratorItemReader<>(memberIds.iterator()) {
            @Override
            public Long read() {
                Long memberId = super.read();
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
        log.info("user base proccessor 실행중---");
        return userId -> {
            log.info("Processor received userId: {}", userId);
            if (userId == null) {
                log.warn("Processor received null userId.");
                return null;
            }

            List<RecommendedItem> recommendations = recommendationService.processUserRecommendations(userId);
            log.info("Processor generated recommendations for userId {}: {}", userId, recommendations);

            return recommendations.stream()
                    .map(item -> PensionRecommendation.builder()
                            .pensionMemberId(new PensionMemberId(item.getItemID(), userId))
                            .score(item.getValue())
                            .build())
                    .toList();
        };
    }

    // 점수 업데이트 + 추천 데이터 생성 Processor
    @Bean
    public ItemProcessor<Long, List<PlaceRecommendation>> combinedProcessor() {
        return pensionId -> {
            pensionPlaceScoreService.updateScoresForPension(pensionId); // 점수 업데이트
            return pensionPlaceScoreService.recommendPlacesForPension(pensionId); // 추천 데이터 생성
        };
    }

    // 사용자 기반 추천 저장 Writer
    @Bean
    public ItemWriter<List<PensionRecommendation>> userRecommendationWriter() {
        log.info("user base writer 실행중 ---");

        return items -> {
            EntityManager entityManager = entityManagerFactory.createEntityManager();
            entityManager.getTransaction().begin(); // 트랜잭션 시작
            try {
                for (List<PensionRecommendation> recommendations : items) { // Chunk 단위
                    for (PensionRecommendation recommendation : recommendations) { // 리스트 단위
                        entityManager.persist(recommendation); // 엔티티 저장
                    }
                }
                entityManager.getTransaction().commit(); // 트랜잭션 커밋
            } catch (Exception e) {
                entityManager.getTransaction().rollback(); // 예외 발생 시 롤백
                throw e;
            } finally {
                entityManager.close(); // EntityManager 닫기
            }
        };
    }


    // 펜션 연관 시설 추천 데이터 저장 Writer
    @Bean
    public JpaItemWriter<List<PlaceRecommendation>> placeRecommendationWriter() {
        return new JpaItemWriterBuilder<List<PlaceRecommendation>>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}

