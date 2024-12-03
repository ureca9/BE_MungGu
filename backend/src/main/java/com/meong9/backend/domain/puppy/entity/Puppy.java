package com.meong9.backend.domain.puppy.entity;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.global.mediafile.entity.MediaFile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Puppy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long puppyId;

    @Column(name = "puppy_name", nullable = false)
    private String name;

    private LocalDate birthDate;

    private Character gender;

    private Double weight;

    private Boolean neutered;

    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "breed_id") // Breed에 대한 외래 키
    private Breed breed;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_image_id") // 외래 키 이름 명시
    @Setter
    private MediaFile profileImage;

    @Builder
    public Puppy(String name, LocalDate birthDate, Character gender, Double weight, Boolean neutered, Breed breed, MediaFile profileImage, Member member) {
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.weight = weight;
        this.neutered = neutered;
        this.breed = breed;
        this.profileImage = profileImage;
        this.member = member;
    }

    // 수정 메서드(이미지 포함)
    public void updateWithImg(String name, LocalDate birthDate, Character gender, Double weight, Boolean neutered, Breed breed, MediaFile profileImageId) {
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.weight = weight;
        this.neutered = neutered;
        this.breed = breed;
        this.profileImage = profileImageId;
    }
    // 수정 메서드(이미지 미포함)
    public void update(String name, LocalDate birthDate, Character gender, Double weight, Boolean neutered, Breed breed) {
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.weight = weight;
        this.neutered = neutered;
        this.breed = breed;
    }

}
