package com.meong9.backend.domain.place.repository;

import com.meong9.backend.domain.place.entity.PlcCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface PlcCategoryRepository extends JpaRepository<PlcCategory, Long> {

    @Query("SELECT pc FROM PlcCategory pc WHERE pc.name IN :names")
    List<PlcCategory> findAllByNameIn(@Param("names") Set<String> names);
}
