package com.meong9.backend.domain.pension.repository;

import com.meong9.backend.domain.pension.dto.PensionInfoDto;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.pension.dto.PensionSummaryResponseDto;
import com.meong9.backend.domain.pension.entity.Pension;
import com.meong9.backend.domain.recommendation.recommendation.projection.PlcPenProjection;
import com.meong9.backend.domain.review.dto.ReviewInfoQueryResult;
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


    @EntityGraph(attributePaths = {"pensionTags.tag", "pensionFiles.mediaFile"})
    @Query("SELECT p FROM Pension p WHERE p.pensionId = :pensionId")
    Optional<Pension> findByPensionId(@Param("pensionId") Long pensionId);

    @Query("""
    SELECT DISTINCT new com.meong9.backend.domain.pension.dto.PensionSummaryResponseDto(
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

    @Query("SELECT new com.meong9.backend.domain.review.dto.ReviewInfoQueryResult(p, COUNT(r)) " +
            "FROM Pension p " +
            "LEFT JOIN Review r ON r.placePensionId = p.pensionId AND r.type = :type " +
            "WHERE p.pensionId = :pensionId " +
            "GROUP BY p")
    Optional<ReviewInfoQueryResult> findByPensionIdWithImageAndReviewCount(@Param("pensionId") Long pensionId,
                                                                         @Param("type") String type);

    @Query("""
    SELECT DISTINCT P,
           CASE WHEN EXISTS (SELECT 1
                FROM PensionLike pl
                JOIN Like l ON pl.likeId = l.likeId
            WHERE l.member.memberId = :memberId AND pl.pension.pensionId = P.pensionId) 
            THEN TRUE ELSE FALSE END AS LIKED 
    FROM Pension P
    LEFT JOIN P.pensionFiles pf
    LEFT JOIN pf.mediaFile
    WHERE P.pensionId IN :pensionIds
    """)
    List<Object[]> findAllWithLikeStatus(
            @Param("pensionIds") List<Long> pensionIds,
            @Param("memberId") Long memberId
    );

    @Query("SELECT p.name FROM Pension p WHERE p.pensionId = :pensionId")
    String findNameByPensionId(@Param("pensionId") Long pensionId); // 이름만 조회

    /**
     * pensionId 목록으로 펜션 정보를 조회합니다.(TopPension 생성을 위해, tags만을 필요로 함)
     *
     * @param pensionIds 조회할 pensionId 목록
     * @return 조회된 Place 리스트
     */
    @Query("SELECT p FROM Pension p LEFT JOIN FETCH p.pensionTags WHERE p.pensionId IN :pensionIds")
    List<Pension> findByPensionIds(@Param("pensionIds") List<Long> pensionIds);

    @Query("SELECT p FROM Pension p WHERE p.pensionId IN :ids")
    List<Pension> findAllByIdIn(@Param("ids") List<Long> ids);

    @Query("SELECT p.pensionId FROM Pension p")
    List<Long> findAllPensionIds();

    @Query("""
        SELECT p.pensionId AS id, p.latitude AS latitude, p.longitude AS longitude 
        FROM Pension p 
    """)
    List<PlcPenProjection> findPensionProjection();
}
