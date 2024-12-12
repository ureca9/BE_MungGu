package com.meong9.backend.domain.meongPhoto.repository;

import com.meong9.backend.domain.meongPhoto.entity.MeongPhoto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MeongPhotoRepository extends JpaRepository<MeongPhoto, Long> {
    @Query("""
                SELECT mp
                FROM MeongPhoto mp
                WHERE (:lastPhotoId IS NULL OR mp.meongPhotoId < :lastPhotoId)
                ORDER BY mp.mediaFile.createdAt DESC
            """)
    List<MeongPhoto> findAllByLastPhotoId(Long lastPhotoId, Pageable pageable);

    @Query("""
                SELECT mp
                FROM MeongPhoto mp
                WHERE mp.member.memberId = :memberId
                  AND (:lastPhotoId IS NULL OR mp.meongPhotoId < :lastPhotoId)
                ORDER BY mp.mediaFile.createdAt DESC
            """)
    List<MeongPhoto> findAllByMemberIdAndLastPhotoId(Long memberId, Long lastPhotoId, Pageable pageable);
}
