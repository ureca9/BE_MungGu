package com.meong9.backend.domain.place.repository;

import com.meong9.backend.domain.pension.dto.TopPensionResponseDto;
import com.meong9.backend.domain.place.dto.PlaceInfoDto;
import com.meong9.backend.domain.place.dto.PlaceSummaryResponseDto;
import com.meong9.backend.domain.place.dto.TopPlaceResponseDto;
import com.meong9.backend.domain.place.entity.Place;
import com.meong9.backend.domain.recommendation.recommendation.projection.PlcPenProjection;
import com.meong9.backend.domain.review.dto.ReviewInfoQueryResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT new com.meong9.backend.domain.review.dto.ReviewInfoQueryResult(p, COUNT(r)) " +
            "FROM Place p " +
            "LEFT JOIN Review r ON r.placePensionId = p.placeId AND r.type = :type " +
            "WHERE p.placeId = :placeId " +
            "GROUP BY p")
    Optional<ReviewInfoQueryResult> findByPlaceIdWithImageAndReviewCount(@Param("placeId") Long placeId,
                                                                         @Param("type") String type);

    @Query(""" 
    SELECT new com.meong9.backend.domain.place.dto.PlaceSummaryResponseDto(
    pl.name,
    pl.reviewAvg,
    pl.reviewCount
    )FROM Place pl
    WHERE pl.placeId = :placeId
    """)
    Optional<PlaceSummaryResponseDto> findPlaceSummaryResponseDtoById(@Param("placeId") Long placeId);

    @Query("""
    SELECT DISTINCT P,
           CASE WHEN EXISTS (SELECT 1
                FROM PlaceLike pl
                JOIN Like l ON pl.likeId = l.likeId
            WHERE l.member.memberId = :memberId AND pl.place.placeId = P.placeId) 
            THEN TRUE ELSE FALSE END AS LIKED 
    FROM Place P
    LEFT JOIN P.placeFiles pf
    LEFT JOIN pf.mediaFile
    WHERE P.placeId IN :placeIds
    """)
    List<Object[]> findAllWithLikeStatus(
            @Param("placeIds") List<Long> placeIds,
            @Param("memberId") Long memberId
    );


    @Query(""" 
    SELECT new com.meong9.backend.domain.place.dto.PlaceInfoDto(
        p.placeId, p.name, p.plcCategory.plcCategoryId, p.plcCategory.name, p.reviewCount, p.reviewAvg,
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

    @Query("SELECT p FROM Place p WHERE p.placeId IN :ids")
    List<Place> findAllByIdIn(@Param("ids") List<Long> ids);

    @Query("SELECT p.placeId FROM Place p")
    List<Long> findAllPlaceIds();

    @Query("""
        SELECT p.placeId AS id, p.latitude AS latitude, p.longitude AS longitude 
        FROM Place p 
        WHERE p.placeId IN :ids
    """)
    List<PlcPenProjection> findPlaceProjectionById(@Param("ids") List<Long> ids);

    @Query("""
            SELECT new com.meong9.backend.domain.place.dto.TopPlaceResponseDto(
        p.placeId,
        p.name,
        p.reviewCount,
        p.reviewAvg,
        a.province,
        a.cityDistrict,
        a.subDistrict,
        mf.fileUrl
    )
    FROM Place p
    LEFT JOIN PlcPenAddress pp ON pp.plcPenId = p.placeId AND pp.type = :type
    LEFT JOIN Address a ON a.addressId = pp.address.addressId
    LEFT JOIN PlaceFile pf ON pf.place.placeId = p.placeId
    LEFT JOIN MediaFile mf ON mf.mediaFileId = pf.mediaFile.mediaFileId AND mf.isDeleted = false
    WHERE p.placeId IN :ids
    AND pf.mediaFile.mediaFileId = (
        SELECT MIN(pf_sub.mediaFile.mediaFileId)
        FROM PlaceFile pf_sub
        WHERE pf_sub.place.placeId = p.placeId
    )
    """)
    List<TopPlaceResponseDto> findPlaceTop(@Param("ids") List<Long> ids, @Param("type") String type);

    @Query("""
    SELECT new com.meong9.backend.domain.place.dto.TopPlaceResponseDto(
        p.placeId,
        p.name,
        p.reviewCount,
        p.reviewAvg,
        a.province,
        a.cityDistrict,
        a.subDistrict,
        mf.fileUrl
    )
    FROM Place p
    LEFT JOIN PlcPenAddress pp ON pp.plcPenId = p.placeId AND pp.type = :type
    LEFT JOIN Address a ON a.addressId = pp.address.addressId
    LEFT JOIN PlaceFile pf ON pf.place.placeId = p.placeId
    LEFT JOIN MediaFile mf ON mf.mediaFileId = pf.mediaFile.mediaFileId AND mf.isDeleted = false
    WHERE pf.mediaFile.mediaFileId = (
        SELECT MIN(pf_sub.mediaFile.mediaFileId)
        FROM PlaceFile pf_sub
        WHERE pf_sub.place.placeId = p.placeId
    )
    ORDER BY p.reviewCount DESC
""")
    Page<TopPlaceResponseDto> findTopPlacesByReviewCount(@Param("type") String type, Pageable pageable);
}
