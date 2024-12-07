package com.meong9.backend.domain.weather.controller;

import com.meong9.backend.domain.weather.dto.WeatherDto;
import com.meong9.backend.domain.weather.service.WeatherApiService;
import com.meong9.backend.domain.weather.service.WeatherService;
import com.meong9.backend.global.dto.CommonResponse;
import com.meong9.backend.global.exception.BadRequestException;
import com.meong9.backend.global.utils.RegionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/weather")
    public ResponseEntity<?> getWeather(@RequestParam(name = "region") String region){
        String regionName = RegionMapper.getWeatherRegion(region); // 지역 코드
        if(regionName == null){
            throw BadRequestException.invalidRegionNameFormat(region);
        }
        WeatherDto weatherDto = weatherService.getWeatherData(region);

        return CommonResponse.ok("success", weatherDto);
    }
}
