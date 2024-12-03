package com.meong9.backend.domain.place.repository;

import com.meong9.backend.domain.place.entity.Place;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;\
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
}
