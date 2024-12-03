package com.meong9.backend.domain.weather.controller;

import com.meong9.backend.domain.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/weather")
    public String getWeather() {
        return weatherService.getWeatherForecast();
    }

    @GetMapping("/weather/2")
    public String getWeather2() {
        return weatherService.getWeatherForecast2();
    }
}
