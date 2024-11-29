package com.meong9.backend.domain.pension.entity;

import com.meong9.backend.domain.pension.entity.id.PensionTagKey;
import com.meong9.backend.global.entity.Tag;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PensionTag {
    @EmbeddedId
    private PensionTagKey id; // 복합 키 객체

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("pensionId") // PlaceTagKey의 placeId와 매핑
    @JoinColumn(name = "pension_id")
    private Pension pension;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tagId") // PlaceTagKey의 tagId와 매핑
    @JoinColumn(name = "tag_id")
    private Tag tag;

    public PensionTag(Pension pension, Tag tag) {
        this.id = new PensionTagKey(pension.getPensionId(), tag.getTagId());
        this.pension = pension;
        this.tag = tag;
    }
}
