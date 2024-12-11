package com.meong9.backend.domain.address.repository;

import com.meong9.backend.domain.address.entity.PlcPenAddress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface PlcPenAddressRepository extends JpaRepository<PlcPenAddress, Long> {

    @Query("""
    SELECT CASE
        WHEN a.address IS NOT NULL AND a.address <> '' THEN a.address
        ELSE CONCAT(a.province, ' ', a.cityDistrict, ' ', a.subDistrict, ' ', COALESCE(a.addressDetail, ''))
    END
    FROM PlcPenAddress ppa
    JOIN ppa.address a
    WHERE ppa.type = :type AND ppa.plcPenId = :plcPenId
    """)
    Optional<String> findFullAddress(@Param("type") String type, @Param("plcPenId") Long plcPenId);

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

    @Query("""
            SELECT ppa.plcPenId FROM PlcPenAddress ppa
            WHERE ppa.address.region.regionId IN :regionIds
            AND ppa.type = :typeCode
            """)
    Slice<Long> findFacilityIdsByRegionIdInWithPagination(@Param("regionIds") List<Long> regionIds, @Param("typeCode") String typeCode, Pageable pageable);

    @Query("""
        SELECT pa FROM PlcPenAddress pa
        LEFT JOIN FETCH pa.address a
        WHERE pa.plcPenId IN :ids AND pa.type = :type
    """)
    List<PlcPenAddress> findAddressesByIdsAndType(
        @Param("ids") List<Long> ids,
        @Param("type") String type
    );

    @Query("""
        SELECT a.region.regionId
        FROM PlcPenAddress ppa
        JOIN Address a ON ppa.address.addressId = a.addressId
        WHERE ppa.plcPenId = :pensionId and ppa.type = "020"
    """)
    Long findRegionIdByPensionId(Long pensionId);

    @Query("""
        SELECT ppa.plcPenId
        FROM PlcPenAddress ppa
        JOIN Address a ON ppa.address.addressId = a.addressId
        WHERE a.region.regionId IN :regionIds
    """)
    List<Long> findPensionIdsByRegionIds(@Param("regionIds") List<Long> regionIds);
}
