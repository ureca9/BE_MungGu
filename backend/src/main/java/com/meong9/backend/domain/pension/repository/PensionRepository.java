package com.meong9.backend.domain.pension.repository;

import com.meong9.backend.domain.pension.entity.Pension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PensionRepository extends JpaRepository<Pension, Long> {
}
