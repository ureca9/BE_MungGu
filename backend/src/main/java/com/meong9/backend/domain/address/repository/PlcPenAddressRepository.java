package com.meong9.backend.domain.address.repository;

import com.meong9.backend.domain.address.entity.PlcPenAddress;
import com.meong9.backend.domain.puppy.entity.Breed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlcPenAddressRepository extends JpaRepository<PlcPenAddress, Long> {
    @Query("""
    SELECT ppa FROM PlcPenAddress ppa
    LEFT JOIN FETCH ppa.address a
    WHERE ppa.type = :type AND ppa.plcPenId = :plcPenId
    """)
    Optional<PlcPenAddress> findPlcPenAddressWithAddress(@Param("type") String type, @Param("plcPenId") Long plcPenId);

}
