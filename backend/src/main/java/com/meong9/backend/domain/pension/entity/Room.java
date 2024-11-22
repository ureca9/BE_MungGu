package com.meong9.backend.domain.pension.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    @Column(name = "room_name")
    private String name;

    private Integer price;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String startTime;

    private String endTime;

    private String area;

    @Column(columnDefinition = "TEXT")
    private String information;

    private Integer guestCount;

    private Integer petCount;

    private Integer basicPrice;

    @Column(nullable = false)
    private Boolean isSoldOut = false;
}
