package com.meong9.backend.domain.pension.repository;

import com.meong9.backend.domain.pension.dto.PensionInfoDto;
import com.meong9.backend.domain.pension.dto.PensionSummaryResponseDto;
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

    @Query("""
    SELECT new com.meong9.backend.domain.pension.dto.PensionInfoDto(
        p.pensionId,
        p.name,
        p.reviewCount,
        p.reviewAvg,
        p.startTime,
        p.endTime,
        p.telNo,
        p.latitude,
        p.longitude,
        p.pensionDescription,
        p.enterPetSize,
        p.info,
        p.introduction,
        p.petLimitInfo,
        CASE
            WHEN EXISTS (
                SELECT 1 FROM PensionLike l
                WHERE l.member.memberId = :memberId
                  AND l.pension.pensionId = p.pensionId
            ) THEN true
            ELSE false
        END
    )
    FROM Pension p
    WHERE p.pensionId = :pensionId
""")
    Optional<PensionInfoDto> findPensionInfoByIdWithLikeStatus(@Param("pensionId") Long pensionId, @Param("memberId") Long memberId);



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
}
