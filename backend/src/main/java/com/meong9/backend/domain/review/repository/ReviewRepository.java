package com.meong9.backend.domain.review.repository;

import com.meong9.backend.domain.review.entity.Review;
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
    """)
    List<Review> findReviewsByPlaceId(@Param("placeId") Long placeId, @Param("type") String type);
}
