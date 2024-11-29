package com.meong9.backend.domain.address.entity;

import com.meong9.backend.global.entity.Region;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId; // 기본 키

    @Column(length = 255, nullable = true)
    private String address; // 도로명 주소

    @Column(name = "province", length = 20, nullable = false)
    private String province; // 주/도

    @Column(name = "city_district", length = 20, nullable = false)
    private String cityDistrict; // 시/군/구

    @Column(name = "subdistrict", length = 20, nullable = false)
    private String subDistrict; // 읍/면/동

    @Column(name = "address_detail", length = 255, nullable = true)
    private String addressDetail; // 상세 주소

    @Column(name = "zip_no", length = 5, nullable = true)
    private String zipNo; // 우편번호

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;
}
