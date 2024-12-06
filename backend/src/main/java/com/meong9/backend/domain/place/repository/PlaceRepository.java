package com.meong9.backend.domain.place.repository;

import com.meong9.backend.domain.member.entity.Member;
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

    @EntityGraph(attributePaths = {"plcCategory", "placeTags.tag", "placeFiles.mediaFile"})
    @Query("SELECT p FROM Place p WHERE p.placeId = :placeId")
    Optional<Place> findPlaceWithDetails(@Param("placeId") Long placeId);


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
        select p, 
               case when (count(l) > 0) then true else false end as liked
        from Place p
        left join p.likes l on l.member = :member
        where p.placeId in :placeIds
        group by p
    """)
    List<Object[]> findAllWithLikeStatus(
            @Param("placeIds") List<Long> placeIds,
            @Param("member") Member member
    );

}
