package com.meong9.backend.domain.meongPhoto.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MyMeongPhotoDto {
    private Long photoId;
    private String meongPhotoUrl;
    private LocalDateTime createdAt;

    @JsonProperty("createdAt")
    public String getFormattedCreatedAt() {
        return createdAt != null
                ? createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : null;
    }
}
