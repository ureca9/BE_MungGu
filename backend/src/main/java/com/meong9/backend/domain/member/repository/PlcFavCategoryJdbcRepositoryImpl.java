package com.meong9.backend.domain.member.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PlcFavCategoryJdbcRepositoryImpl implements PlcFavCategoryJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    public void batchInsert(List<Object[]> batchParams) {
        String sql = "INSERT INTO plc_fav_category (member_id, plc_category_id) VALUES (?, ?)";
        jdbcTemplate.batchUpdate(sql, batchParams);
    }
}
