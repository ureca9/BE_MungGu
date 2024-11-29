package com.meong9.backend.domain.address.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class PlcPenAddress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long plcPenAddressId;

    @Column(name = "type", length = 3)
    private String type;

    private Long plcPenId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;
}
