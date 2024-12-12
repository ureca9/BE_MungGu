package com.meong9.backend.domain.place.repository;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.place.dto.PlaceInfoDto;
import com.meong9.backend.domain.place.dto.PlaceSummaryResponseDto;
import com.meong9.backend.domain.place.entity.Place;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

    @Query("""
        SELECT pl
        FROM Place pl
        LEFT JOIN FETCH pl.placeFiles plf
        LEFT JOIN FETCH plf.mediaFile
        WHERE pl.placeId IN :placeIds
    """)
    List<Place> findAllDataByIds(@Param("placeIds") List<Long> placeIds);

    @EntityGraph(attributePaths = {"placeFiles.mediaFile"})
    @Query("SELECT p FROM Place p WHERE p.placeId = :placeId")
    Optional<Place> findByPlaceIdWithImage(@Param("placeId")Long id);

    @Query(""" 
    SELECT new com.meong9.backend.domain.place.dto.PlaceSummaryResponseDto(
    pl.name,
    pl.reviewAvg,
    pl.reviewCount
    )FROM Place pl
    WHERE pl.placeId = :placeId
    """)
    Optional<PlaceSummaryResponseDto> findPlaceSummaryResponseDtoById(@Param("placeId") Long placeId);

    @EntityGraph(attributePaths = {"placeFiles.mediaFile"})
    @Query("""
        SELECT P, 
               CASE WHEN (COUNT(L) > 0) THEN TRUE ELSE FALSE END AS LIKED
        FROM Place P
        LEFT JOIN P.likes L ON L.member = :member
        WHERE P.placeId IN :placeIds
        GROUP BY P
    """)
    List<Object[]> findAllWithLikeStatus(
            @Param("placeIds") List<Long> placeIds,
            @Param("member") Member member
    );


    @Query(""" 
    SELECT new com.meong9.backend.domain.place.dto.PlaceInfoDto(
        p.placeId, p.name, p.plcCategory.name, p.reviewCount, p.reviewAvg,
        p.businessHour, p.telNo, p.hmpgUrl,
        p.latitude, p.longitude, p.closedDays,
        p.priceContent, p.petLimitInfo,
        p.plcDescription, p.enterPetSize,
        CASE
            WHEN EXISTS (
                SELECT 1 FROM PlaceLike l
                WHERE l.member.memberId = :memberId
                  AND l.place.placeId = p.placeId
            ) THEN true
            ELSE false
        END
    )
    FROM Place p
    WHERE p.placeId = :placeId
""")
    Optional<PlaceInfoDto> findPlaceInfoById(@Param("placeId") Long placeId, @Param("memberId") Long memberId);
}
