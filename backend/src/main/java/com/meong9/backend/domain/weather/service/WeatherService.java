package com.meong9.backend.domain.weather.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WebClient webClient;

    @Value("${weather.mid.decoding-key}")
    private String weatherMidKey; // 중기 예보 키

//    public String getWeatherForecast() {
//        // API 호출
//        return webClient.get()
//                .uri(uriBuilder -> uriBuilder
////                        .path("/1360000/MidFcstInfoService/getMidFcst")
////                        .path("/1360000/MidFcstInfoService")
////                        .path("/1360000/MidFcstInfoService/getMidLandFcst") // 중기 육상 예보
//                        .path("/1360000/VilageFcstInfoService_2.0/getVilageFcst")
//                        .queryParam("serviceKey", weatherMidKey) // 서비스키 입력
//                        .queryParam("pageNo", "1")
//                        .queryParam("numOfRows", "10")
//                        .queryParam("dataType", "JSON") // JSON 형식으로 요청
//                        .queryParam("regId", "11B00000") // 전국 예보
//                        .queryParam("tmFc", "202412020600") // YYYYMMDDHHMM
//                        .build())
//                .retrieve()
//
//                .bodyToMono(String.class) // 응답을 String으로 받음
//                .block(); // 동기 방식으로 처리
//    }

    public String getWeatherForecast() {
        // API 호출
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
//                        .path("/1360000/MidFcstInfoService/getMidFcst")
//                        .path("/1360000/MidFcstInfoService")
//                        .path("/1360000/MidFcstInfoService/getMidLandFcst") // 중기 육상 예보
                        .path("/1360000/VilageFcstInfoService_2.0/getVilageFcst")
                        .queryParam("serviceKey", weatherMidKey) // 서비스키 입력
                        .queryParam("pageNo", "1")
                        .queryParam("numOfRows", "100")
                        .queryParam("dataType", "JSON") // JSON 형식으로 요청
                        .queryParam("base_date", "20241202")
                        .queryParam("base_time", "0500")
                        .queryParam("nx", "55")
                        .queryParam("ny", "127")
                        .build())
                .retrieve()

                .bodyToMono(String.class) // 응답을 String으로 받음
                .block(); // 동기 방식으로 처리
    }
}

