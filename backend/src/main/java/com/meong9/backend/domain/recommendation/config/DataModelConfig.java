package com.meong9.backend.domain.recommendation.config;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.model.jdbc.ReloadFromJDBCDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@Slf4j
public class DataModelConfig {

    private final DataSource dataSource;

    public DataModelConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // 사용자-펜션 데이터 모델 (펜션 추천)
    @Bean(name = "pensionDataModel")
    public DataModel pensionDataModel() throws Exception {
        ReloadFromJDBCDataModel dataModel = new ReloadFromJDBCDataModel(
                new MySQLJDBCDataModel(
                        dataSource,
                        "pension_member_score", // 테이블 이름
                        "member_id",            // 회원 ID 컬럼
                        "pension_id",           // 펜션 ID 컬럼
                        "score",                // 점수 컬럼
                        "last_updated_at"       // 타임스탬프 컬럼
                )
        );

        log.info("Validating data model connection...");
        LongPrimitiveIterator userIDs = dataModel.getUserIDs();
        try {
            if (!userIDs.hasNext()) {
                log.warn("No data found in pension_member_score table.");
            } else {
                while (userIDs.hasNext()) {
                    log.info("User ID from data model: {}", userIDs.nextLong());
                }
            }
        } finally {
            // LongPrimitiveIterator는 AutoCloseable이 아니므로 명시적으로 닫지 않습니다.
        }

        return dataModel;
    }

    // 펜션-시설 데이터 모델 (시설 추천)
    @Bean(name = "placeDataModel")
    public DataModel placeDataModel() throws Exception {
        ReloadFromJDBCDataModel dataModel = new ReloadFromJDBCDataModel(
                new MySQLJDBCDataModel(
                        dataSource,
                        "pension_place_score", // 테이블 이름
                        "pension_id",                  // 펜션 ID 컬럼
                        "place_id",                   // 시설 ID 컬럼
                        "score",                   // 점수 컬럼
                        "last_updated_at" // 타임스탬프
                )
        );

        log.info("Validating data model connection...");
        LongPrimitiveIterator userIDs = dataModel.getUserIDs();
        try {
            if (!userIDs.hasNext()) {
                log.warn("No data found in pension_member_score table.");
            } else {
                while (userIDs.hasNext()) {
                    log.info("User ID from data model: {}", userIDs.nextLong());
                }
            }
        } finally {
            // LongPrimitiveIterator는 AutoCloseable이 아니므로 명시적으로 닫지 않습니다.
        }

        return dataModel;
    }



    // 펜션-시설 데이터 모델 (시설 추천)
//    @Bean(name = "placeDataModel")
//    public DataModel facilityDataModel() throws Exception {
//        return new ReloadFromJDBCDataModel(
//                new MySQLJDBCDataModel(
//                        dataSource,
//                        "pension_place_score", // 테이블 이름
//                        "pension_id",                  // 펜션 ID 컬럼
//                        "place_id",                   // 시설 ID 컬럼
//                        "score",                   // 점수 컬럼
//                        "last_updated_at" // 타임스탬프
//                )
//        );
//
//
//    }
}