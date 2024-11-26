package com.meong9.backend.global.repository;

import com.meong9.backend.global.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface RegionRepository extends JpaRepository<Region, Long> {
    @Query("SELECT r FROM Region r WHERE r.name IN :names")
    List<Region> findAllByNameIn(@Param("names") Set<String> newRegionsNames);
}
