package com.meong9.backend.domain.meongPhoto.repository;

import com.meong9.backend.domain.meongPhoto.dto.MeongPhotoDto;
import com.meong9.backend.domain.meongPhoto.dto.MyMeongPhotoDto;
import com.meong9.backend.domain.meongPhoto.entity.MeongPhoto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MeongPhotoRepository extends JpaRepository<MeongPhoto, Long> {

    @Query("""
            SELECT new com.meong9.backend.domain.meongPhoto.dto.MeongPhotoDto(
            mp.meongPhotoId,
            m.nickname,
            m.profileImage.fileUrl,
            mf.fileUrl,
            mf.createdAt)
            FROM MeongPhoto mp
            JOIN mp.member m
            JOIN mp.mediaFile mf
            ORDER BY mp.mediaFile.createdAt DESC
            """)
    Slice<MeongPhotoDto> findAllWithPagination(Pageable pageable);

    @Query("""
            SELECT new com.meong9.backend.domain.meongPhoto.dto.MyMeongPhotoDto(
            mp.meongPhotoId,
            mf.fileUrl,
            mf.createdAt)
            FROM MeongPhoto mp
            JOIN mp.member m
            JOIN mp.mediaFile mf
            WHERE m.memberId = :memberId
            ORDER BY mp.mediaFile.createdAt DESC
            """)
    Slice<MyMeongPhotoDto> findAllByMemberIdWithPagination(Long memberId, Pageable pageable);
}
