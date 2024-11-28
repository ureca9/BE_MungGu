package com.meong9.backend.domain.review.entity;

import com.meong9.backend.domain.review.entity.id.ReviewFileId;
import com.meong9.backend.global.entity.BaseTimeEntity;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReviewFile{

    @EmbeddedId
    private ReviewFileId id; // 복합 키

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("reviewId") // ReviewFileId의 reviewId와 매핑
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("mediaFileId") // ReviewFileId의 mediaFileId와 매핑
    @JoinColumn(name = "media_file_id", nullable = false)
    private MediaFile file;

    public ReviewFile(Review review, MediaFile file) {
        this.id = new ReviewFileId(review.getReviewId(), file.getMediaFileId());
        this.review = review;
        this.file = file;
    }
}
