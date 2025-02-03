package com.meong9.backend.domain.review.repository;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.review.dto.FileResponseDto;
import com.meong9.backend.domain.review.dto.MyReviewResponseDto;
import com.meong9.backend.domain.review.dto.PhotoReviewSummaryResponseDto;
import com.meong9.backend.domain.review.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("SELECT r.placePensionId FROM Review r where r.type = '020' GROUP BY r.placePensionId HAVING COUNT(r.reviewId) > :count")
    List<Long> findPensionReviewCount(@Param("count") int count);

    @Query("""
            SELECT DISTINCT r
            FROM Review r
            LEFT JOIN FETCH r.reviewFiles rf
            LEFT JOIN FETCH rf.file
            ORDER BY r.createdAt DESC LIMIT 10
            """)
    List<Review> findTop10RecentReviews();

    @Query("""
                SELECT new com.meong9.backend.domain.review.dto.MyReviewResponseDto(
                    r.reviewId, 
                    r.content, 
                    r.score, 
                    r.visitDate, 
                    r.type, 
                    r.placePensionId, 
                    CASE 
                        WHEN r.type = '010' THEN p.name 
                        WHEN r.type = '020' THEN ps.name 
                        ELSE null 
                    END, 
                    r.nickname,
                    new com.meong9.backend.domain.review.dto.FileResponseDto(
                        rf.review.reviewId,
                        mf.fileType,
                        mf.fileSize,
                        mf.fileUrl,
                        mf.fileName
                    )
                ) 
                FROM Review r 
                LEFT JOIN Place p 
                    ON r.placePensionId = p.placeId 
                    AND r.type = '010' 
                LEFT JOIN Pension ps 
                    ON r.placePensionId = ps.pensionId 
                    AND r.type = '020' 
                LEFT JOIN ReviewFile rf 
                    ON r.reviewId = rf.review.reviewId 
                    AND rf.file.mediaFileId = (
                        SELECT MIN(rf2.file.mediaFileId) 
                        FROM ReviewFile rf2 
                        WHERE rf2.review.reviewId = r.reviewId
                    )
                LEFT JOIN MediaFile mf 
                    ON rf.file.mediaFileId = mf.mediaFileId 
                WHERE r.member = :member
            """)
    List<MyReviewResponseDto> findReviewsByMember(@Param("member") Member member);


    @Query("""
                SELECT new com.meong9.backend.domain.review.dto.FileResponseDto(
                    rf.review.reviewId, 
                    mf.fileType, 
                    mf.fileSize, 
                    mf.fileUrl, 
                    mf.fileName
                ) 
                FROM ReviewFile rf 
                JOIN rf.file mf 
                WHERE rf.review.reviewId IN :reviewIds
            """)
    List<FileResponseDto> findFilesByReviewIds(@Param("reviewIds") List<Long> reviewIds);


    @Query("""
            SELECT r FROM Review r
            LEFT JOIN FETCH r.member m
            LEFT JOIN FETCH m.profileImage pi
            LEFT JOIN FETCH r.reviewFiles rf
            LEFT JOIN FETCH rf.file f
            WHERE r.placePensionId = :placeId AND r.type = :type
            ORDER BY r.createdAt DESC
            """)
    List<Review> findReviewsByPlaceId(@Param("placeId") Long placeId, @Param("type") String type, Pageable pageable);

    @Query("""
                SELECT new com.meong9.backend.domain.review.dto.PhotoReviewSummaryResponseDto(
                    r.reviewId,
                    MIN(rf.file.fileUrl),
                    COUNT(rf.file.fileUrl)
                )
                FROM Review r
                JOIN r.reviewFiles rf
                WHERE r.placePensionId = :placeId AND r.type = :type
                GROUP BY r.reviewId
                ORDER BY r.createdAt DESC
            """)
    Slice<PhotoReviewSummaryResponseDto> findPhotoReviewSummaries(@Param("placeId") Long placeId, @Param("type") String type, Pageable pageable);

    @Query("""
                SELECT r
                FROM Review r
                WHERE r.type = :type AND r.placePensionId = :placePensionId
                ORDER BY r.visitDate DESC
            """)
    Slice<Review> findByTypeAndPlacePensionId(
            @Param("type") String type,
            @Param("placePensionId") Long placePensionId,
            Pageable pageable
    );
}
