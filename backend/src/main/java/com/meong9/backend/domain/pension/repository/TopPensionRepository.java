package com.meong9.backend.domain.pension.repository;

import com.meong9.backend.domain.pension.dto.TopPensionResponseDto;
import com.meong9.backend.domain.pension.entity.TopPension;
import com.meong9.backend.domain.place.dto.TopPlaceResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopPensionRepository extends JpaRepository<TopPension, Long> {
    @Query(value = """
        SELECT new com.meong9.backend.domain.pension.dto.TopPensionResponseDto(
            t.pensionId, t.pensionName, t.reviewCount, t.reviewAvg,
            t.province, t.cityDistrict, t.subDistrict, t.viewCount)
        FROM TopPension t
        WHERE (t.year = :startYear AND t.month = :startMonth AND t.date >= :startDate)
        OR (t.year = :endYear AND t.month = :endMonth AND t.date <= :endDate)
        OR (t.year = :startYear AND t.month > :startMonth)
        OR (t.year = :endYear AND t.month < :endMonth)
        OR (t.year > :startYear AND t.year < :endYear)
        GROUP BY t.pensionId, t.pensionName, t.reviewCount, t.reviewAvg,
                t.province, t.cityDistrict, t.subDistrict, t.viewCount
        ORDER BY SUM(t.viewCount) DESC
        """)
    Slice<TopPensionResponseDto> findTop9PensionsByDateRangeAndCategory(
            @Param("startYear") Integer startYear,
            @Param("startMonth") Integer startMonth,
            @Param("startDate") Integer startDate,
            @Param("endYear") Integer endYear,
            @Param("endMonth") Integer endMonth,
            @Param("endDate") Integer endDate,
            Pageable pageable);


    @Query("""
    SELECT pf.pension.pensionId AS pensionId, mf.fileUrl AS fileUrl
    FROM PensionFile pf
    JOIN pf.mediaFile mf
    WHERE pf.pension.pensionId IN :pensionIds
    AND pf.pensionFileId.mediaFileId = (
        SELECT MIN(pf2.pensionFileId.mediaFileId)
        FROM PensionFile pf2
        WHERE pf2.pension.pensionId = pf.pension.pensionId
    )
    """)
    List<Object[]> findRepresentativeImagesByPensionIds(@Param("pensionIds") List<Long> pensionIds);

}
