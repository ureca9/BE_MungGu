package com.meong9.backend.domain.place.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200, name = "place_name")
    private String name;

    private String latitude; // 위도

    private String longitude; // 경도

    @Column(length = 20)
    private String telNo;

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

    @Column(nullable = false, precision = 2, scale = 1)
    private Double reviewAvg = 0.0; // 정수부 + 소수부 합쳐서 2자리. 소수부 1자리

    @Column(nullable = false)
    private Integer likeCount = 0;
}
