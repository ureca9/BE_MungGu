package com.meong9.backend.domain.like.entity;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.place.entity.Place;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("Place")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlaceLike extends Like{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;

    public PlaceLike(Member member, Place place) {
        super(null, member);
        this.place = place;
    }
}
