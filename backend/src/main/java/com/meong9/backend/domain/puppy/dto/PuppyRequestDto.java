package com.meong9.backend.domain.puppy.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class PuppyRequestDto {
    private Long breedId;
    private String name;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    private Character gender;
    private Double weight;
    private Boolean neutered;
}