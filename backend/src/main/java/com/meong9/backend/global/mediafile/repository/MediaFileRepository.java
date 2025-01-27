package com.meong9.backend.global.mediafile.repository;

import com.meong9.backend.global.mediafile.entity.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {
    @Query("""
        select mf from MediaFile mf
        where mf.fileUrl = :fileUrl
        order by mf.createdAt desc limit 1
    """)
    Optional<MediaFile> findByFileUrl(String fileUrl);
}
