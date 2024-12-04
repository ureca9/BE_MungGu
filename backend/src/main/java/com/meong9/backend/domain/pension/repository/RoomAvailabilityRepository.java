package com.meong9.backend.domain.pension.repository;

import com.meong9.backend.domain.pension.entity.RoomAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomAvailabilityRepository extends JpaRepository<RoomAvailability, Long> {
}
