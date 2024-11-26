package com.meong9.backend.domain.puppy.repository;

import com.meong9.backend.domain.puppy.entity.Breed;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BreedRepository extends JpaRepository<Breed, Long> {
}
