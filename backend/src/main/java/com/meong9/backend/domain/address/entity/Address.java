package com.meong9.backend.domain.address.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId; // 기본 키

    @Column(length = 255, nullable = true)
    private String address; // 도로명 주소

    @Column(length = 20, nullable = false)
    private String province; // 주/도

    @Column(name = "city_district", length = 20, nullable = false)
    private String cityDistrict; // 시/군/구

    @Column(length = 20, nullable = false)
    private String subdistrict; // 읍/면/동

    @Column(name = "address_detail", length = 255, nullable = true)
    private String addressDetail; // 상세 주소

    @Column(name = "zip_no", length = 5, nullable = true)
    private String zipNo; // 우편번호
}