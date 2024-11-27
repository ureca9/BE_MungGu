package com.meong9.backend.domain.puppy.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class PuppyProfileResponseDto {

    private Long puppyId;
    private String name;
    private LocalDate birthDate;
    private Character gender;
    private Double weight;
    private Boolean neutered;
    private Long breedId;
    private String breedName;
    private String profileImageUrl;

    // 명시적 생성자
    public PuppyProfileResponseDto(Long puppyId, String name, LocalDate birthDate, Character gender,
                                   Double weight, Boolean neutered, Long breedId, String breedName,
                                   String profileImageUrl) {
        this.puppyId = puppyId;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.weight = weight;
        this.neutered = neutered;
        this.breedId = breedId;
        this.breedName = breedName;
        this.profileImageUrl = profileImageUrl;
    }
}


