package com.meong9.backend.domain.address.repository;

import com.meong9.backend.domain.address.entity.PlcPenAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlcPenAddressRepository extends JpaRepository<PlcPenAddress, Long> {
    @Query("select ppa from PlcPenAddress ppa where ppa.plcPenId =:plcPenId and ppa.type =:type")
    PlcPenAddress findByPlcPenIdAndType(@Param("plcPenId")Long plcPenId, @Param("type")String type);
}
