package com.meong9.backend.domain.pension.repository;

import com.meong9.backend.domain.pension.entity.PensionFile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PensionFileRepository extends CrudRepository<PensionFile, Long> {
    @Query("""
    SELECT mf.fileUrl
    FROM PensionFile pf
    JOIN pf.mediaFile mf
    WHERE pf.pension.pensionId = :pensionId
""")
    List<String> findImagesByPensionId(@Param("pensionId") Long pensionId);
}
