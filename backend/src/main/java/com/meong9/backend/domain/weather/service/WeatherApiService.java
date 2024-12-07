package com.meong9.backend.domain.weather.service;

import com.meong9.backend.global.exception.BadRequestException;
import com.meong9.backend.global.exception.InternalServerError;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;



@Service
@RequiredArgsConstructor
public class WeatherApiService {

    private final WebClient webClient;

    @Value("${weather.mid.encoding-key}")
    private String weatherMidKey; // 중기 예보 키

    // 단기 예보
    public String getWeatherForecastST(String[] xy, String date) {
        try {
            // URL 포맷 지정
            String url = String.format(
                    "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst?serviceKey=%s&pageNo=1&numOfRows=798&dataType=JSON&base_date=%s&base_time=1400&nx=%s&ny=%s",
                    weatherMidKey, date, xy[0], xy[1]
            );

            // WebClient 호출
            String responseBody = webClient.get()
                    .uri(URI.create(url)) // 자동 인코딩 방지
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // JSON 형식 검증
            if (!responseBody.trim().startsWith("{") && !responseBody.trim().startsWith("[")) {
                throw InternalServerError.invalidWeatherResponseFormat(responseBody);
            }

            return responseBody;
        } catch (Exception e) {
            throw InternalServerError.weatherApiError(e.getMessage());
        }
    }

    // 중기 예보
    public String getWeatherForecastMid(String code, String date) {
        try {
            // URL 포맷 지정
            String url = String.format(
                    "http://apis.data.go.kr/1360000/MidFcstInfoService/getMidLandFcst?serviceKey=%s&pageNo=1&numOfRows=10&dataType=JSON&regId=%s&tmFc=%s",
                    weatherMidKey, code, date
            );

            // WebClient 호출
            String responseBody = webClient.get()
                    .uri(URI.create(url)) // 자동 인코딩 방지
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // JSON 형식 검증
            if (!responseBody.trim().startsWith("{") && !responseBody.trim().startsWith("[")) {
                throw InternalServerError.invalidWeatherResponseFormat(responseBody);
            }

            return responseBody;
        } catch (Exception e) {
            throw InternalServerError.weatherApiError(e.getMessage());
        }
    }
}

