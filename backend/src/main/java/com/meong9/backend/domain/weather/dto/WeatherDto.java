package com.meong9.backend.domain.weather.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class WeatherDto {
    @Setter
    private String regionName;

    private String day1;
    private String day2;
    private String day3;
    private String day4;
    private String day5;
    private String day6;
    private String day7;
    private String day8;
    private String day9;
    private String day10;
}
