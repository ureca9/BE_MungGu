package com.meong9.backend.domain.plc_pen_review.repository;

import com.meong9.backend.domain.plc_pen_review.entity.PlcPenReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlcPenReviewRepository extends JpaRepository<PlcPenReview, Long> {
    List<PlcPenReview> findByType(String number);
}
