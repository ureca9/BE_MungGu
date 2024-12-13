package com.meong9.backend.domain.pension.repository;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.pension.dto.PensionSummaryResponseDto;
import com.meong9.backend.domain.pension.entity.Pension;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
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

    @Query("""
    SELECT new com.meong9.backend.domain.pension.dto.PensionSummaryResponseDto(
    p.name,
    p.reviewAvg,
    p.reviewCount
    )
    FROM Pension p
    WHERE p.pensionId = :pensionId
    """)
    Optional<PensionSummaryResponseDto> findPensionSummaryResponseDtoById(@Param("pensionId") Long pensionId);

    @EntityGraph(attributePaths = {"pensionFiles.mediaFile"})
    @Query("SELECT p FROM Pension p WHERE p.pensionId = :pensionId")
    Optional<Pension> findByPensionIdWithImage(@Param("pensionId") Long pensionId);

    @EntityGraph(attributePaths = {"pensionFiles.mediaFile"})
    @Query("""
    SELECT P, 
           CASE WHEN EXISTS (SELECT 1
                FROM PensionLike pl
                JOIN Like l ON pl.likeId = l.likeId
            WHERE l.member.memberId = :memberId AND pl.pension.pensionId = P.pensionId) 
            THEN TRUE ELSE FALSE END AS LIKED 
    FROM Pension P
    WHERE P.pensionId IN :pensionIds
    """)
    List<Object[]> findAllWithLikeStatus(
            @Param("pensionIds") List<Long> pensionIds,
            @Param("memberId") Long memberId
    );

    @Query("SELECT p.name FROM Pension p WHERE p.pensionId = :pensionId")
    String findNameByPensionId(@Param("pensionId") Long pensionId); // 이름만 조회
}
