package com.meong9.backend.domain.place.repository;

import com.meong9.backend.domain.place.entity.PlaceFile;
import com.meong9.backend.domain.place.entity.id.PlaceFileId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceFileRepository extends JpaRepository<PlaceFile, PlaceFileId> {

}
