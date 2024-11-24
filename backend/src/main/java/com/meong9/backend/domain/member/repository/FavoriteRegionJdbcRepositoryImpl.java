package com.meong9.backend.domain.member.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
@RequiredArgsConstructor
public class FavoriteRegionJdbcRepositoryImpl implements FavoriteRegionJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public void batchInsert(List<Object[]> batchParams) {
        String sql = "INSERT INTO favorite_region (member_id, region_id) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(sql, batchParams);
    }
}
