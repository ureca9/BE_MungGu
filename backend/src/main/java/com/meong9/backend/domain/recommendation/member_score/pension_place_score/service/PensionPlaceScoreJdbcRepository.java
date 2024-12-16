package com.meong9.backend.domain.recommendation.member_score.pension_place_score.service;

import com.meong9.backend.domain.recommendation.recommendation.dto.PensionPlaceScoreDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PensionPlaceScoreJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void batchInsertOrUpdate(List<PensionPlaceScoreDto> scores) {
        String sql = "INSERT INTO pension_place_score (pension_id, place_id, score, last_updated_at) " +
                "VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "score = VALUES(score), last_updated_at = VALUES(last_updated_at)";

        jdbcTemplate.batchUpdate(sql, scores, 20, (ps, score) -> {
            ps.setLong(1, score.getPensionPlaceId().getPensionId());
            ps.setLong(2, score.getPensionPlaceId().getPlaceId());
            ps.setFloat(3, score.getScore());
            ps.setTimestamp(4, Timestamp.valueOf(score.getLastUpdatedAt()));
        });
    }
}
