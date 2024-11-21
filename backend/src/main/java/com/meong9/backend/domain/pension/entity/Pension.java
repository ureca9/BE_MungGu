package com.meong9.backend.domain.pension.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Pension {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(nullable = false, precision = 2, scale = 1)
    private Double reviewAvg = 0.0; // 정수부 + 소수부 합쳐서 2자리. 소수부 1자리

    private String startTime;

    private String endTime;

    @Column(nullable = false)
    private boolean isSoldOut = false;
}