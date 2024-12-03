package com.meong9.backend.domain.weather.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WebClient webClient;

    @Value("${weather.mid.encoding-key}")
    private String weatherMidKey; // 중기 예보 키

    public String getWeatherForecast() {
        try {
            // URL 빌드
            StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1360000/MidFcstInfoService/getMidLandFcst");
            urlBuilder.append("?").append("serviceKey").append("=").append(weatherMidKey);
            urlBuilder.append("&").append(URLEncoder.encode("pageNo", "UTF-8")).append("=").append(URLEncoder.encode("1", "UTF-8")); // 페이지 번호
            urlBuilder.append("&").append(URLEncoder.encode("numOfRows", "UTF-8")).append("=").append(URLEncoder.encode("10", "UTF-8")); // 한 페이지 결과 수
            urlBuilder.append("&").append(URLEncoder.encode("dataType", "UTF-8")).append("=").append(URLEncoder.encode("JSON", "UTF-8")); // 데이터 형식
            urlBuilder.append("&").append(URLEncoder.encode("regId", "UTF-8")).append("=").append(URLEncoder.encode("11B00000", "UTF-8")); // 지역 코드
            urlBuilder.append("&").append(URLEncoder.encode("tmFc", "UTF-8")).append("=").append(URLEncoder.encode("202412030600", "UTF-8")); // 발표 시각

            // URL 객체 생성
            URL url = new URL(urlBuilder.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");

            System.out.println("Request URL: " + urlBuilder.toString());

            // 응답 코드 확인
            int responseCode = conn.getResponseCode();
            System.out.println("Response code: " + responseCode);

            // 응답 처리
            BufferedReader rd;
            if (responseCode >= 200 && responseCode <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
            }
            rd.close();
            conn.disconnect();

            // 응답 반환
            return response.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to call Weather API", e);
        }
    }

    public String getWeatherForecast2() {
        try {
            // URL 빌드
            StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst");
            urlBuilder.append("?").append("serviceKey").append("=").append(weatherMidKey);
            urlBuilder.append("&").append(URLEncoder.encode("pageNo", "UTF-8")).append("=").append(URLEncoder.encode("1", "UTF-8")); // 페이지 번호
            urlBuilder.append("&").append(URLEncoder.encode("numOfRows", "UTF-8")).append("=").append(URLEncoder.encode("10", "UTF-8")); // 한 페이지 결과 수
            urlBuilder.append("&").append(URLEncoder.encode("dataType", "UTF-8")).append("=").append(URLEncoder.encode("JSON", "UTF-8")); // 데이터 형식
            urlBuilder.append("&").append(URLEncoder.encode("base_date", "UTF-8")).append("=").append(URLEncoder.encode("20241203", "UTF-8")); // 발표 날짜
            urlBuilder.append("&").append(URLEncoder.encode("base_time", "UTF-8")).append("=").append(URLEncoder.encode("0500", "UTF-8")); // 발표 시간
            urlBuilder.append("&").append(URLEncoder.encode("nx", "UTF-8")).append("=").append(URLEncoder.encode("55", "UTF-8")); // 예보 지점 X 좌표 값
            urlBuilder.append("&").append(URLEncoder.encode("ny", "UTF-8")).append("=").append(URLEncoder.encode("127", "UTF-8")); // 예보 지점 Y 좌표 값

            // URL 객체 생성
            URL url = new URL(urlBuilder.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");

            System.out.println("Request URL: " + urlBuilder.toString());

            // 응답 코드 확인
            int responseCode = conn.getResponseCode();
            System.out.println("Response code: " + responseCode);

            // 응답 처리
            BufferedReader rd;
            if (responseCode >= 200 && responseCode <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
            }
            rd.close();
            conn.disconnect();

            // 응답 반환
            return response.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to call Weather API", e);
        }
    }
}

