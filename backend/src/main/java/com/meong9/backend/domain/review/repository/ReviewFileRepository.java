package com.meong9.backend.domain.review.repository;

import com.meong9.backend.domain.review.dto.FileResponseDto;
import com.meong9.backend.domain.review.entity.Review;
import com.meong9.backend.domain.review.entity.ReviewFile;
import com.meong9.backend.domain.review.entity.id.ReviewFileId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ReviewFileRepository extends JpaRepository<ReviewFile, ReviewFileId> {
    List<ReviewFile> findByReview(Review review);

    @Query("""
    SELECT rf
    FROM ReviewFile rf
    JOIN FETCH rf.file
    WHERE rf.review.reviewId IN :reviewIds
""")
    List<ReviewFile> findFilesByReviewIds(@Param("reviewIds") List<Long> reviewIds);

    @Query("""
    SELECT rf
    FROM ReviewFile rf
    JOIN FETCH rf.file
    WHERE rf.file.mediaFileId = :mediaFileId
""")
    Optional<ReviewFile> findByMediaFileId(@Param("mediaFileId") Long mediaFileId);

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
    List<FileResponseDto> findMediaFilesByReviewIds(List<Long> reviewIds);
}
