package com.meong9.backend.domain.place.repository;

import com.meong9.backend.domain.place.entity.PlaceTag;
import com.meong9.backend.domain.place.entity.id.PlaceTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceTagRepository extends JpaRepository<PlaceTag, PlaceTagId> {
    @Query("""
    SELECT t.name
    FROM PlaceTag pt
    JOIN pt.tag t
    WHERE pt.place.placeId = :placeId
    """)
    List<String> findNamesByPlaceId(@Param("placeId") Long placeId);

}
