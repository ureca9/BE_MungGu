package com.meong9.backend.domain.review.entity;

import com.meong9.backend.domain.review.entity.id.ReviewFileId;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReviewFile{ // 복합 키 클래스
    @EmbeddedId
    private ReviewFileId id; // 복합 키

    @ManyToOne
    @MapsId("reviewId") // 복합 키와 매핑
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId("mediaFileId") // 복합 키와 매핑
    @JoinColumn(name = "media_file_id", nullable = false)
    private MediaFile file;

    @Setter
    private String status = "UPLOADED";

    @Builder
    public ReviewFile(Review review, MediaFile file,ReviewFileId reviewFileId) {
        this.review = review;
        this.file = file;
        this.id = reviewFileId;
    }
}