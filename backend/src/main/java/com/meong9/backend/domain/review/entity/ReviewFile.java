package com.meong9.backend.domain.review.entity;

import com.meong9.backend.global.entity.BaseTimeEntity;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import jakarta.persistence.*;
import lombok.*;

import com.meong9.backend.global.entity.BaseTimeEntity;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "review_file")
@ToString
public class ReviewFile{ // 복합 키 클래스
    @EmbeddedId
    private ReviewFileId id; // 복합 키

    @ManyToOne
    @MapsId("reviewId") // 복합 키와 매핑
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("mediaFileId") // 복합 키와 매핑
    @JoinColumn(name = "media_file_id", nullable = false)
    private MediaFile file;

    public ReviewFile(Review review, MediaFile file) {
        this.review = review;
        this.file = file;
    }
}