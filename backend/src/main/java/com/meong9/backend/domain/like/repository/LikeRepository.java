package com.meong9.backend.domain.like.repository;

import com.meong9.backend.domain.like.entity.Like;
import com.meong9.backend.domain.like.entity.PensionLike;
import com.meong9.backend.domain.like.entity.PlaceLike;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.pension.entity.Pension;
import com.meong9.backend.domain.place.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<PlaceLike> findByMemberAndPlace(Member member, Place place);
    Optional<PensionLike> findByMemberAndPension(Member member, Pension pension);
}
