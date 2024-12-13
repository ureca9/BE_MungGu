package com.meong9.backend.global.topFeature.entity;

import com.meong9.backend.domain.pension.entity.Pension;
import com.meong9.backend.domain.pension.entity.PensionFeature;
import com.meong9.backend.domain.place.entity.PlaceFeature;
import com.meong9.backend.global.utils.TagToFeatureMapping;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "top_feature")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TopFeature {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long topFeatureId;

    private Boolean parking = false;
    private Boolean petOnlyArea = false;
    private Boolean indoorSpace = false;
    private Boolean outdoorSpace = false;
    private Boolean weightLimit = false;
    private Boolean swimmingPool = false;
    private Boolean barbecue = false;
    private Boolean bulmeong = false;
    private Boolean fence = false;
    private Boolean barking = false;
    private Boolean noSmoking = false;

    @OneToMany(mappedBy = "topFeature", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlaceFeature> placeFeatures = new ArrayList<>();

    @OneToMany(mappedBy = "topFeature", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PensionFeature> pensionFeatures = new ArrayList<>();

    // `TagToFeatureMapping`을 기반으로 설정하는 메서드 추가
    public void applyTag(Long tagId) {
        TagToFeatureMapping.applyFeature(tagId, this);
    }
}
