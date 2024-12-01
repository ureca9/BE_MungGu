package com.meong9.backend.domain.review.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;


@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ReviewFileId implements Serializable {
    @Column(name = "review_id")
    private Long reviewId;
    @Column(name = "media_file_id")
    private Long mediaFileId;
}
