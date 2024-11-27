package com.meong9.backend.domain.plc_pen_review.repository;

import com.meong9.backend.domain.plc_pen_review.entity.PlcPenReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlcPenReviewRepository extends JpaRepository<PlcPenReview, Long> {
    List<PlcPenReview> findByType(String number);

    @Query("SELECT r.plcPenId FROM PlcPenReview r where r.type = '020' GROUP BY r.plcPenId HAVING COUNT(r.plcPenReviewId) > :count")
    List<Long> findPensionReviewCount(@Param("count") int count);
}
