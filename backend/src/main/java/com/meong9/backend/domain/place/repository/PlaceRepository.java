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

import java.util.Collection;
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
           CASE WHEN EXISTS (SELECT 1
                FROM PlaceLike pl
                JOIN Like l ON pl.likeId = l.likeId
            WHERE l.member.memberId = :memberId AND pl.place.placeId = P.placeId) 
            THEN TRUE ELSE FALSE END AS LIKED 
    FROM Place P
    WHERE P.placeId IN :placeIds
    """)
    List<Object[]> findAllWithLikeStatus(
            @Param("placeIds") List<Long> placeIds,
            @Param("memberId") Long memberId
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

    /**
     * placeId 목록으로 장소 정보를 조회합니다.
     *
     * @param placeIds 조회할 placeId 목록
     * @return 조회된 Place 리스트
     */
    @Query("SELECT p FROM Place p LEFT JOIN FETCH p.placeTags WHERE p.placeId IN :placeIds")
    List<Place> findByPlaceIds(@Param("placeIds") List<Long> placeIds);

    @Query("SELECT p.name FROM Place p WHERE p.placeId = :placeId")
    String findNameByPlaceId(@Param("placeId") Long placeId); // 이름만 조회
    @Query("SELECT c.plcCategoryId FROM Place p JOIN p.plcCategory c WHERE p.placeId = :placeId")
    List<Long> findCategoryIdsByPensionId(@Param("placeId") Long placeId);

    @Query("SELECT p FROM Place p WHERE p.placeId IN :ids")
    List<Place> findAllByIdIn(@Param("ids") List<Long> ids);
}
