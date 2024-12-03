package com.meong9.backend.domain.pension.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Builder
public class Pension {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pensionId;

    @Column(name = "pension_name")
    private String name;

    private String telNo;

    private String enterPetSize;

    @Column(columnDefinition = "TEXT")
    private String pensionDescription;

    @Column(columnDefinition = "TEXT")
    private String info;

    @Column(columnDefinition = "TEXT")
    private String petLimitInfo;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Column(nullable = false)
    private Integer reviewCount = 0;

    @Column(nullable = false)
    private Double reviewAvg = 0.0; // 정수부 + 소수부 합쳐서 2자리. 소수부 1자리

    private String startTime;

    private String endTime;

    @Column(nullable = false)
    private Boolean isSoldOut = false;

    @Column(nullable = false)
    private Integer likeCount = 0;

    @OneToMany(mappedBy = "pension", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<PensionTag> pensionTags = new HashSet<>();

    @Column(name = "latitude", length = 20)
    private String latitude;

    @Column(name = "longitude", length = 20)
    private String longitude;

    @OneToMany(mappedBy = "pension", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PensionFile> pensionFiles = Collections.emptyList(); // 펜션 파일 리스트

    public void increaseLikeCount(){
        this.likeCount++;
    }

    public void decreaseLikeCount(){
        this.likeCount=Math.max(this.likeCount-1,0);
    }
}
