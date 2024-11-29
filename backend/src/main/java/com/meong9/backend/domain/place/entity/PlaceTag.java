package com.meong9.backend.domain.place.entity;

import com.meong9.backend.domain.place.entity.id.PlaceTagKey;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlaceTag {

    @EmbeddedId
    private PlaceTagKey id; // 복합 키 객체

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("placeId") // PlaceTagKey의 placeId와 매핑
    @JoinColumn(name = "place_id")
    private Place place;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @MapsId("tagId") // PlaceTagKey의 tagId와 매핑
//    @JoinColumn(name = "tag_id")
//    private Tag tag;
//
//    public PlaceTag(Place place, Tag tag) {
//        this.id = new PlaceTagKey(place.getPlaceId(), tag.getTagId());
//        this.place = place;
//        this.tag = tag;
//    }
}
