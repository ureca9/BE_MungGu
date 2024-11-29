package com.meong9.backend.domain.review.repository;

import com.meong9.backend.domain.review.entity.ReviewFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewFileRepository extends JpaRepository<ReviewFile, Long> {
}
