package com.meong9.backend.domain.review.repository;

import com.meong9.backend.domain.review.dto.PhotoReviewSummaryResponseDto;
import com.meong9.backend.domain.review.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("""
    SELECT r FROM Review r
    LEFT JOIN FETCH r.reviewFiles rf
    LEFT JOIN FETCH rf.file f
    WHERE r.placePensionId = :placeId and r.type = :type
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
    List<PhotoReviewSummaryResponseDto> findPhotoReviewSummaries( @Param("placeId") Long placeId,@Param("type") String type);
}
