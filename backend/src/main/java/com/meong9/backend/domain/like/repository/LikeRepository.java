package com.meong9.backend.domain.like.repository;

import com.meong9.backend.domain.like.entity.Like;
import com.meong9.backend.domain.like.entity.PensionLike;
import com.meong9.backend.domain.like.entity.PlaceLike;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.pension.entity.Pension;
import com.meong9.backend.domain.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<PlaceLike> findByMemberAndPlace(Member member, Place place);
    Optional<PensionLike> findByMemberAndPension(Member member, Pension pension);

    // Place 즐겨찾기 상태 확인
    @Query("SELECT COUNT(*) > 0 FROM PlaceLike pl WHERE pl.member = :member AND pl.place.placeId = :placeId")
    boolean existsByMemberAndPlaceId(@Param("member") Member member, @Param("placeId") Long placeId);

    // Pension 즐겨찾기 상태 확인
    @Query("SELECT COUNT(*) > 0 FROM PensionLike pl WHERE pl.member = :member AND pl.pension.pensionId = :pensionId")
    boolean existsByMemberAndPensionId(@Param("member") Member member, @Param("pensionId") Long pensionId);
}
