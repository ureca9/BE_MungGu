package com.meong9.backend.domain.member.repository;

import com.meong9.backend.domain.member.entity.FavoriteRegion;
import com.meong9.backend.domain.member.entity.id.FavoriteRegionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavoriteRegionRepository2 extends JpaRepository<FavoriteRegion, FavoriteRegionId> {
    @Query("SELECT f.region.regionId FROM FavoriteRegion f where f.member.memberId = :memberId")
    List<Long> findRegionIdsByMemberId(@Param("memberId") Long memberId);
}
