package com.meong9.backend.domain.place.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class PlcCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long plcCategoryId;

    @Column(name = "plc_category_name")
    private String name;
}
