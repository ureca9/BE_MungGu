package com.meong9.backend.domain.member.entity;

import com.meong9.backend.domain.member.entity.id.FavoriteRegionId;
import com.meong9.backend.global.entity.Region;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class FavoriteRegion {
    @EmbeddedId
    private FavoriteRegionId favoriteRegionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("regionId")
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;
}
