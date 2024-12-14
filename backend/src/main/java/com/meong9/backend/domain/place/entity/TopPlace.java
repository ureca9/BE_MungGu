package com.meong9.backend.domain.place.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "top_place")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TopPlace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long topPlaceId;

    private Long placeId;
    private String placeName;
    private Integer reviewCount;
    private BigDecimal reviewAvg;
    private Integer likeCount;
    private String category;
    private String province;
    private String cityDistrict;
    private String subDistrict;
    private Long viewCount;

    @Column(name = "`rank`")
    private Integer rank;
    private Integer year;
    private Integer month;
    private Integer date;

    @OneToMany(mappedBy = "topPlace", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaceFeature> placeFeatures = new ArrayList<>();
}