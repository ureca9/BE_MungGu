package com.meong9.backend.domain.plc_pen_review.entity;

import com.meong9.backend.domain.review.entity.Review;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PlcPenReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long plcPenReviewId;
    private String type;
    private Long plcPenId;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "review_id")
    private Review review;

}
