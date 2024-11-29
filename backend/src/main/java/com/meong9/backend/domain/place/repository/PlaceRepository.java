package com.meong9.backend.domain.place.repository;

import com.meong9.backend.domain.place.entity.Place;
import com.meong9.backend.domain.search.dto.SearchPlaceDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
    @Query("""
            SELECT new com.meong9.backend.domain.search.dto.SearchPlaceDto(
            p.placeId,
            p.name,
            NULL,
            p.plcCategory.name,
            NULL,
            p.reviewAvg,
            p.reviewCount,
            p.enterPetSize,
            p.businessHour,
            NULL,
            NULL)
            FROM Place p
            WHERE p.placeId IN :regionCondition
            AND p.plcCategory.plcCategoryId IN :categoryCondition
            AND (p.enterPetSize = '030' OR
            (p.enterPetSize = '020' AND :sizeCode IN ('020', '010')) OR
            (p.enterPetSize = '010' AND :sizeCode = '010'))
            """)
    Slice<SearchPlaceDto> findPlacesByCondition(@Param("regionCondition") List<Long> regionCondition,
                                                   @Param("categoryCondition") List<Long> categoryCondition,
                                                   @Param("sizeCode") String sizeCode,
                                                   Pageable pageable);


}
