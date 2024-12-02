package com.meong9.backend.domain.place.repository;

import com.meong9.backend.domain.place.entity.PlaceTag;
import com.meong9.backend.domain.place.entity.id.PlaceTagKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceTagRepository extends JpaRepository<PlaceTag, PlaceTagKey> {

}
