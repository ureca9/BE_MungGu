package com.meong9.backend.domain.address.repository;

import com.meong9.backend.domain.address.entity.PlcPenAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.List;
import java.util.Optional;

public interface PlcPenAddressRepository extends JpaRepository<PlcPenAddress, Long> {
    @Query("select ppa from PlcPenAddress ppa where ppa.plcPenId =:plcPenId and ppa.type =:type")
    Optional<PlcPenAddress> findByPlcPenIdAndType(@Param("plcPenId")Long plcPenId, @Param("type")String type);



    @Query("""
        SELECT pa FROM PlcPenAddress pa
        LEFT JOIN FETCH pa.address a
        WHERE (pa.plcPenId IN :pensionIds AND pa.type = '020') 
           OR (pa.plcPenId IN :placeIds AND pa.type = '010')
    """)
    List<PlcPenAddress> findByPlcPenIdInAndType(
            @Param("pensionIds") List<Long> pensionIds,
            @Param("placeIds") List<Long> placeIds
    );

    @Query("""
            SELECT ppa.plcPenId FROM PlcPenAddress ppa
            WHERE ppa.address.region.regionId IN :regionIds
            AND ppa.type = :typeCode
            """)
    List<Long> findFacilityIdsByRegionIdIn(@Param("regionIds") List<Long> regionIds, @Param("typeCode") String typeCode);

}
