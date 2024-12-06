package com.meong9.backend.domain.place.entity;

import com.meong9.backend.domain.like.entity.PlaceLike;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long placeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plc_category_id")
    private PlcCategory plcCategory;

    @Column(nullable = false, length = 200, name = "place_name")
    private String name;

    private String latitude; // 위도

    private String longitude; // 경도

    @Column(length = 20)
    private String telNo;

    @Column(columnDefinition = "TEXT")
    private String hmpgUrl;

    private String closedDays;

    @Column(columnDefinition = "TEXT")
    private String priceContent; // 이용가격 내용

    @Column(length = 200)
    private String enterPetSize; // 입장가능 반려동물 크기

    @Column(columnDefinition = "TEXT")
    private String petLimitInfo;

    @Column(columnDefinition = "TEXT")
    private String plcDescription;

    private String businessHour;

    @Column(nullable = false)
    private Integer reviewCount = 0;

    @Column(nullable = false)
    private Double reviewAvg = 0.0; // 정수부 + 소수부 합쳐서 2자리. 소수부 1자리

    @Column(nullable = false)
    private Integer likeCount = 0;

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PlaceTag> placeTags = new HashSet<>();

    @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaceFile> placeFiles = new ArrayList<>();

    @OneToMany(mappedBy = "place", fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<PlaceLike> likes = new HashSet<>();

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        this.likeCount = Math.max(this.likeCount - 1, 0);
    }
}
