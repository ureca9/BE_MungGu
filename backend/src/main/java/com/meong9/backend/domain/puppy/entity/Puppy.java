package com.meong9.backend.domain.puppy.entity;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
public class Puppy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long puppyId;

    @Column(name = "puppy_name")
    private String name;

    private LocalDate birthDate;

    private Character gender;

    private Double weight;

    private Boolean neutered;

    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @OneToOne
    private Breed breed;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private MediaFile profileImageId;
}
