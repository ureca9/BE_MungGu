package com.meong9.backend.domain.like.repository;

import com.meong9.backend.domain.like.entity.Like;
import com.meong9.backend.domain.like.entity.PensionLike;
import com.meong9.backend.domain.like.entity.PlaceLike;
import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.pension.entity.Pension;
import com.meong9.backend.domain.place.entity.Place;
import com.meong9.backend.domain.place.entity.PlcCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<PlaceLike> findByMemberAndPlace(Member member, Place place);
    Optional<PensionLike> findByMemberAndPension(Member member, Pension pension);

    // Place 즐겨찾기 상태 확인
    @Query("""
            SELECT CASE 
                WHEN COUNT(pl) > 0 
                THEN true 
                ELSE false 
                END
            FROM PlaceLike pl
            WHERE pl.member = :member AND pl.place.placeId = :placeId
            """)
    boolean existsByMemberAndPlaceId(@Param("member") Member member, @Param("placeId") Long placeId);

    // Pension 즐겨찾기 상태 확인
    @Query("""
            SELECT CASE 
                WHEN COUNT(pl) > 0 
                THEN true 
                ELSE false 
                END
            FROM PensionLike pl
            WHERE pl.member = :member AND pl.pension.pensionId = :pensionId
            """)
    boolean existsByMemberAndPensionId(@Param("member") Member member, @Param("pensionId") Long pensionId);

    // 찜 펜션 목록 조회
    @Query("SELECT pl FROM PensionLike pl " +
            "JOIN FETCH pl.pension pen " +
            "WHERE pl.member = :member")
    List<PensionLike> findAllPensionLikes(@Param("member") Member member);


    @EntityGraph(attributePaths = {
            "pension"
    })
    @Query("SELECT pl FROM PensionLike pl " +
            "WHERE pl.member = :member")
    Page<PensionLike> findAllPensionLikesPage(@Param("member") Member member, Pageable pageable);

    // 찜 시설 목록 조회
    @Query("SELECT pl FROM PlaceLike pl " +
            "JOIN FETCH pl.place pla " +
            "WHERE pl.member = :member")
    List<PlaceLike> findAllPlaceLikes(@Param("member") Member member);

    @EntityGraph(attributePaths = {
            "place"
    })
    @Query("SELECT pl FROM PlaceLike pl " +
            "WHERE pl.member = :member")
    Page<PlaceLike> findAllPlaceLikesPage(@Param("member") Member member, Pageable pageable);

    // 특정 카테고리 좋아요 목록
    @EntityGraph(attributePaths = {
            "place"
    })
    @Query("SELECT pl FROM PlaceLike pl " +
            "WHERE pl.member = :member AND pl.place.plcCategory = :plcCategory")
    Page<PlaceLike> findPlaceLikesByCategory(@Param("member") Member member, @Param("plcCategory") PlcCategory plcCategory, Pageable pageable);



}
