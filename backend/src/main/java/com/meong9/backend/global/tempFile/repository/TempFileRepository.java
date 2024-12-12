package com.meong9.backend.global.tempFile.repository;

import com.meong9.backend.global.tempFile.entity.TempFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TempFileRepository extends JpaRepository<TempFile, Long> {
}
