package com.meong9.backend.global.tempFile.repository;

import com.meong9.backend.global.tempFile.entity.TempFile;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TempFileRepository extends JpaRepository<TempFile, Long> {
    @Query("""
                SELECT tf
                FROM TempFile tf
                WHERE tf.serviceUrl = :serviceUrl
                  AND tf.tempFileId > :lastProcessedId
                ORDER BY tf.tempFileId ASC
            """)
    List<TempFile> findAllByServiceUrlAndLastId(String serviceUrl, Long lastProcessedId, Pageable pageable);
}
