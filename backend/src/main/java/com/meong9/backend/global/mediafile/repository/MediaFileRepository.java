package com.meong9.backend.global.mediafile.repository;

import com.meong9.backend.global.mediafile.entity.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {
}
