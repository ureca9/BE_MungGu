package com.meong9.backend.global.mediafile.repository;

import com.meong9.backend.global.mediafile.entity.EncodingFailure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EncodingFailureRepository extends JpaRepository<EncodingFailure, Long> {
    List<EncodingFailure> findByReviewId(Long reviewId);
}
