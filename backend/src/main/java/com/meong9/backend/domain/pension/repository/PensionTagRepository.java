package com.meong9.backend.domain.pension.repository;

import com.meong9.backend.domain.pension.entity.PensionTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PensionTagRepository extends JpaRepository<PensionTag, Long> {
    @Query("""
    SELECT t.name
    FROM PensionTag pt
    JOIN pt.tag t
    WHERE pt.pension.pensionId = :pensionId
""")
    List<String> findTagsByPensionId(@Param("pensionId") Long pensionId);
}
