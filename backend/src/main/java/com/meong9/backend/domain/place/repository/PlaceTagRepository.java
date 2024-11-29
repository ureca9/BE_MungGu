package com.meong9.backend.domain.place.repository;

import com.meong9.backend.domain.place.entity.PlaceTag;
import com.meong9.backend.domain.place.entity.id.PlaceTagKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceTagRepository extends JpaRepository<PlaceTag, PlaceTagKey> {
    @Query("SELECT pt.id.placeId, t.name " +
            "FROM PlaceTag pt " +
            "JOIN Tag t ON pt.id.tagId = t.tagId " +
            "WHERE pt.place.placeId IN :placeIds")
    List<Object[]> findPlaceTagsByPlaceIds(@Param("placeIds") List<Long> placeIds);

}
