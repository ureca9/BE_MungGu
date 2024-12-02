package com.meong9.backend.domain.pension.entity;

import com.meong9.backend.domain.pension.entity.id.PensionTagId;
import com.meong9.backend.global.entity.Tag;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class PensionTag {
    @EmbeddedId
    private PensionTagId pensionTagId;

    @MapsId("pensionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pension_id")
    private Pension pension;

    @MapsId("tagId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id")
    private Tag tag;
}
