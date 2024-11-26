package com.meong9.backend.domain.member.repository;

import com.meong9.backend.domain.member.entity.FavoriteRegion;
import com.meong9.backend.domain.member.entity.id.FavoriteRegionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FavoriteRegionRepository extends JpaRepository<FavoriteRegion, FavoriteRegionId>, FavoriteRegionJdbcRepository{
    @Modifying
    @Query("DELETE FROM FavoriteRegion fr WHERE fr.member.memberId = :memberId")
    void deleteByMemberId(Long memberId);
}
