package com.meong9.backend.domain.review.repository;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("SELECT r.placePensionId FROM Review r where r.type = '020' GROUP BY r.placePensionId HAVING COUNT(r.reviewId) > :count")
    List<Long> findPensionReviewCount(@Param("count") int count);

    @Query("SELECT r FROM Review r ORDER BY r.createdAt DESC")
    List<Review> findTop10RecentReviews();


    List<Review> findByMember(Member member);
}
