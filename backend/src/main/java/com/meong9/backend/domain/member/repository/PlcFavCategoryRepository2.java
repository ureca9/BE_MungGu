package com.meong9.backend.domain.member.repository;

import com.meong9.backend.domain.member.entity.PlcFavCategory;
import com.meong9.backend.domain.member.entity.id.PlcFavCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlcFavCategoryRepository2 extends JpaRepository<PlcFavCategory, PlcFavCategoryId> {
    @Query("SELECT p.plcCategory.plcCategoryId FROM PlcFavCategory p where p.member.memberId = :memberId")
    List<Long> findCategoryIdsByMemberId(@Param("memberId") Long memberId);
}
