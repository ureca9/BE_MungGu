package com.meong9.backend.domain.place.repository;

import com.meong9.backend.domain.place.dto.TopPlaceResponseDto;
import com.meong9.backend.domain.place.entity.TopPlace;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopPlaceRepository extends JpaRepository<TopPlace,Long> {
    /**
     * 특정 기간 동안의 카테고리별 상위 9개 장소를 조회수 기준으로 조회
     *
     * @param startYear 조회 시작 년도
     * @param startMonth 조회 시작 월
     * @param startDate 조회 시작일
     * @param endYear 조회 종료 년도
     * @param endMonth 조회 종료 월
     * @param endDate 조회 종료일
     * @param category 장소 카테고리
     * @param pageable 페이징 정보
     * @return 상위 장소 목록과 다음 페이지 존재 여부
     */
    @Query(value = """
        SELECT new com.meong9.backend.domain.place.dto.TopPlaceResponseDto(
            t.placeId, t.placeName, t.reviewCount, t.reviewAvg,
            t.province, t.cityDistrict, t.subDistrict, t.viewCount)
        FROM TopPlace t
        WHERE
            (
                (t.year = :startYear AND t.month = :startMonth AND t.date >= :startDate)\s
                OR (t.year = :endYear AND t.month = :endMonth AND t.date <= :endDate)\s
                OR (t.year = :startYear AND t.month > :startMonth)\s
                OR (t.year = :endYear AND t.month < :endMonth)\s
                OR (t.year > :startYear AND t.year < :endYear)
            )
            AND t.category = :category
        GROUP BY t.placeId, t.placeName, t.reviewCount, t.reviewAvg,
                t.province, t.cityDistrict, t.subDistrict, t.viewCount
        ORDER BY SUM(t.viewCount) DESC
        """)
    Slice<TopPlaceResponseDto> findTop9PlacesByDateRangeAndCategory(
            @Param("startYear") Integer startYear,
            @Param("startMonth") Integer startMonth,
            @Param("startDate") Integer startDate,
            @Param("endYear") Integer endYear,
            @Param("endMonth") Integer endMonth,
            @Param("endDate") Integer endDate,
            @Param("category") String category,
            Pageable pageable);

    @Query("""
    SELECT pf.place.placeId AS placeId, mf.fileUrl AS fileUrl
    FROM PlaceFile pf
    JOIN pf.mediaFile mf
    WHERE pf.place.placeId IN :placeIds
    AND pf.placeFileId.mediaFileId = (
        SELECT MIN(pf2.placeFileId.mediaFileId)
        FROM PlaceFile pf2
        WHERE pf2.place.placeId = pf.place.placeId
    )
    """)
    List<Object[]> findRepresentativeImagesByPlaceIds(@Param("placeIds") List<Long> placeIds);

}
