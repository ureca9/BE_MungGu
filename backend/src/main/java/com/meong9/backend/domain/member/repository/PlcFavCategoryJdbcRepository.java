package com.meong9.backend.domain.member.repository;

import java.util.List;

public interface PlcFavCategoryJdbcRepository {
    void batchInsert(List<Object[]> batchParams);
}
