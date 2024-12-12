package com.meong9.backend.domain.place.repository;

import com.meong9.backend.domain.place.entity.PlaceFile;
import com.meong9.backend.domain.place.entity.id.PlaceFileId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceFileRepository extends JpaRepository<PlaceFile, PlaceFileId> {
    @Query("""
    SELECT mf.fileUrl
    FROM PlaceFile pf
    JOIN pf.mediaFile mf
    WHERE pf.place.placeId = :placeId
""")
    List<String> findImagesByPensionId(@Param("placeId") Long placeId);

}
