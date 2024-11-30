package com.meong9.backend.domain.address.entity;

import com.meong9.backend.global.entity.Region;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    private String address;

    @Column(name = "province", length = 20)
    private String province;

    @Column(name = "city_district", length = 20)
    private String cityDistrict;

    @Column(name = "subdistrict", length = 20)
    private String subDistrict;

    private String addressDetail;

    @Column(name = "zip_no", length = 5)
    private String zipNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private Region region;
}
