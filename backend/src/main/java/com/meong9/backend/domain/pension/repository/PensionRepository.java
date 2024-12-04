package com.meong9.backend.domain.pension.repository;

import com.meong9.backend.domain.pension.entity.Pension;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import java.util.Optional;

@Repository
public interface PensionRepository extends JpaRepository<Pension, Long> {

    @Query("""
    SELECT p 
        FROM Pension p
        LEFT JOIN FETCH p.pensionFiles pf
        LEFT JOIN FETCH pf.mediaFile
        WHERE p.pensionId IN :pensionIds
    """)
    List<Pension> findAllDataByIds(@Param("pensionIds") List<Long> pensionIds);

    @EntityGraph(attributePaths = {"pensionTags.tag", "pensionFiles.mediaFile"})
    @Query("SELECT p FROM Pension p WHERE p.pensionId = :pensionId")
    Optional<Pension> findByPensionId(@Param("pensionId") Long pensionId);
}
