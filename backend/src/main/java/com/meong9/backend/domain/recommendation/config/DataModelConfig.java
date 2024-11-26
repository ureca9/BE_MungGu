package com.meong9.backend.domain.recommendation.config;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.model.jdbc.MySQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.model.jdbc.ReloadFromJDBCDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DataModelConfig {

    private final DataSource dataSource;

    public DataModelConfig(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // 사용자-펜션 데이터 모델 (펜션 추천)
    @Bean(name = "pensionDataModel")
    public DataModel pensionDataModel() throws Exception {
        return new ReloadFromJDBCDataModel(
                new MySQLJDBCDataModel(
                        dataSource,
                        "pension_member_score", // 테이블 이름
                        "member_id",            // 회원 ID 컬럼
                        "pension_id",           // 펜션 ID 컬럼
                        "score",                // 점수 컬럼
                        "timestamp"      // 타임스탬프 컬럼
                )
        );
    }

    // 사용자-시설 데이터 모델 (시설 추천)
    @Bean(name = "placeDataModel")
    public DataModel facilityDataModel() throws Exception {
        return new ReloadFromJDBCDataModel(
                new MySQLJDBCDataModel(
                        dataSource,
                        "place_member_score", // 테이블 이름
                        "member_id",                  // 회원 ID 컬럼
                        "place_id",                   // 시설 ID 컬럼
                        "score",                   // 점수 컬럼
                        "timestamp" // 타임스탬프
                )
        );
    }
}