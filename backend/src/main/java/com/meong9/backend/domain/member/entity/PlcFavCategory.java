package com.meong9.backend.domain.member.entity;

import com.meong9.backend.domain.member.entity.id.PlcFavCategoryId;
import com.meong9.backend.domain.place.entity.PlcCategory;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class PlcFavCategory {
    @EmbeddedId
    private PlcFavCategoryId plcFavCategoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("plcCategoryId")
    @JoinColumn(name = "plc_category_id", nullable = false)
    private PlcCategory plcCategory;
}
