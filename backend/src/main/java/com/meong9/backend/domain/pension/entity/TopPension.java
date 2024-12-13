package com.meong9.backend.domain.pension.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "top_pension")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TopPension {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long topPensionId;

    private Long pensionId;
    private String pensionName;
    private Integer reviewCount;
    private BigDecimal reviewAvg;
    private Integer likeCount;
    private String province;
    private String cityDistrict;
    private String subDistrict;
    private Long viewCount;
    @Column(name = "`rank`")
    private Integer rank;
    private Integer year;
    private Integer month;
    private Integer date;
    @DecimalMin("0.0")
    @DecimalMax("5.0")
    private BigDecimal roomPriceAvg;

    @OneToMany(mappedBy = "topPension", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PensionFeature> pensionFeatures = new ArrayList<>();
}