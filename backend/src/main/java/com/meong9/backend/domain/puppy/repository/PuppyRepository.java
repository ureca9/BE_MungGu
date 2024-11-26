package com.meong9.backend.domain.puppy.repository;

import com.meong9.backend.domain.puppy.dto.PuppyProfileResponseDto;
import com.meong9.backend.domain.puppy.entity.Puppy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PuppyRepository extends JpaRepository<Puppy, Long> {

    @Query("SELECT new com.meong9.backend.domain.puppy.dto.PuppyProfileResponseDto(" +
            "p.puppyId, " +
            "p.name, " +
            "p.birthDate, " +
            "p.gender, " +
            "p.weight, " +
            "p.neutered, " +
            "p.breed.breedId, " +
            "p.breed.name, " +
            "p.profileImageId.fileUrl) " +
            "FROM Puppy p " +
            "LEFT JOIN p.breed " +
            "LEFT JOIN p.profileImageId " +
            "WHERE p.puppyId = :puppyId")
    Optional<PuppyProfileResponseDto> findPuppyProfileById(@Param("puppyId") Long puppyId);

}
