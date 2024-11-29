package com.meong9.backend.domain.address.repository;

import com.meong9.backend.domain.address.entity.PlcPenAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlcPenAddressRepository extends JpaRepository<PlcPenAddress, Long> {
    @Query("select ppa from PlcPenAddress ppa where ppa.plcPenId =:plcPenId and ppa.type =:type")
    Optional<PlcPenAddress> findByPlcPenIdAndType(@Param("plcPenId")Long plcPenId, @Param("type")String type);

    @Query("""
            SELECT ppa.plcPenId FROM PlcPenAddress ppa
            WHERE ppa.address.region.regionId IN :regionIds
            AND ppa.type = :typeCode
            """)
    List<Long> findPlaceIdsByRegionIdIn(@Param("regionIds") List<Long> regionIds, @Param("typeCode") String typeCode);

    @Query("""
            SELECT ppa.plcPenId, ppa.address.address
            FROM PlcPenAddress ppa
            WHERE ppa.plcPenId IN :placeIds AND ppa.type = :typeCode
            """)
    List<Object[]> findAddressesByPlaceIdsAndType(@Param("placeIds") List<Long> placeIds, @Param("typeCode") String typeCode);

}
