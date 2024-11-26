package com.meong9.backend.domain.member.repository;

import com.meong9.backend.domain.member.entity.PlcFavCategory;
import com.meong9.backend.domain.member.entity.id.PlcFavCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlcFavCategoryRepository extends JpaRepository<PlcFavCategory, PlcFavCategoryId>, PlcFavCategoryJdbcRepository{
    @Modifying
    @Query("DELETE FROM PlcFavCategory p WHERE p.member.memberId = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);

}
