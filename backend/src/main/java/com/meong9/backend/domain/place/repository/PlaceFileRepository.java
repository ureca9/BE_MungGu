package com.meong9.backend.domain.place.repository;

import com.meong9.backend.domain.place.entity.PlaceFile;
import com.meong9.backend.domain.place.entity.id.PlaceFileId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlaceFileRepository extends JpaRepository<PlaceFile, PlaceFileId> {
    @Query("SELECT pf.place.placeId, mf.fileUrl " +
            "FROM PlaceFile pf " +
            "JOIN MediaFile mf ON mf.mediaFileId = pf.mediaFile.mediaFileId " +
            "WHERE pf.place.placeId IN :placeIds")
    List<Object[]> findImagesByPlaceIds(@Param("placeIds") List<Long> placeIds);

}
