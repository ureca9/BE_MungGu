package com.meong9.backend.domain.meongPhoto.repository;

import com.meong9.backend.domain.meongPhoto.entity.MeongPhoto;
import com.meong9.backend.domain.meongPhoto.entity.id.MeongPhotoId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeongPhotoRepository extends JpaRepository<MeongPhoto, MeongPhotoId> {
}
