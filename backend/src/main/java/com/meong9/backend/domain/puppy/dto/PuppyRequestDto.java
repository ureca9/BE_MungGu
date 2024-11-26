package com.meong9.backend.domain.puppy.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import jakarta.validation.constraints.*;


@Data
public class PuppyRequestDto {

    @NotNull(message = "견종 ID는 필수 입력 값입니다.")
    private Long breedId;

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    @Size(min = 2, max = 20, message = "이름은 2자 이상 20자 이하여야 합니다.")
    private String name;

    @NotNull(message = "생년월일은 필수 입력 값입니다.")
    @PastOrPresent(message = "생년월일은 과거 또는 오늘 날짜만 입력할 수 있습니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @NotNull(message = "성별은 필수 입력 값입니다.")
    @Pattern(regexp = "[MF]", message = "성별은 'M'(수컷) 또는 'F'(암컷)만 입력 가능합니다.")
    private Character gender;

    @NotNull(message = "몸무게는 필수 입력 값입니다.")
    @DecimalMin(value = "0.1", message = "몸무게는 0.1kg 이상이어야 합니다.")
    @DecimalMax(value = "100.0", message = "몸무게는 100kg 이하이어야 합니다.")
    private Double weight;

    @NotNull(message = "중성화 여부는 필수 입력 값입니다.")
    private Boolean neutered;
}
