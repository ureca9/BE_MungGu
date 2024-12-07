package com.meong9.backend.domain.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.meong9.backend.domain.weather.dto.WeatherDto;
import com.meong9.backend.global.exception.InternalServerError;
import com.meong9.backend.global.exception.NotFoundException;
import com.meong9.backend.global.utils.RegionMapper;
import com.meong9.backend.global.utils.WeatherMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    private static final String WEATHER_KEY = "weather:";
    private static final DateTimeFormatter FULL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final WeatherApiService weatherApiService;

    // 지역 별 날씨 데이터 (기상청  api 호출)
    @Scheduled(cron = "0 0 6 * * ?") // 새벽 6시
//    @Scheduled(cron = "0 * * * * ?")
    public void fetchAndStoreWeatherData() {
        for (Map.Entry<String, String> entry : RegionMapper.getWeatherRegionAll().entrySet()) {
            retryProcessRegionWeather(entry.getKey(), entry.getValue(), 3); // 최대 3번 재시도
        }
    }

    // 날씨 데이터 조회
    @Transactional(readOnly = true)
    public WeatherDto getWeatherData(String region) {
        try {
            // redis 키
            String key = String.format("weather:%s", RegionMapper.getRegionEng(region));

            String jsonString = (String) redisTemplate.opsForValue().get(key);

            // redis에 데이터 없으면
            if(jsonString == null){
                throw NotFoundException.entityNotFound(region);
            }

            WeatherDto weatherDto = objectMapper.readValue(jsonString, WeatherDto.class);
            weatherDto.setRegionName(region);

            return weatherDto;
        } catch (Exception e) {
            throw InternalServerError.redisMappingError(e.getMessage());
        }
    }


    // 스케줄러 실패 시 3번 더 해보기
    private void retryProcessRegionWeather(String regionKey, String regionValue, int maxRetries) {
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                processRegionWeather(regionKey, regionValue); // 지역 날씨 처리
                return; // 성공 시 메서드 종료
            } catch (Exception e) {
                attempt++;
                log.info(String.format("지역 처리 실패: %s (%d/%d 시도)", regionKey, attempt, maxRetries));
                if (attempt >= maxRetries) {
                    log.error("최대 재시도 횟수 초과: " + regionKey);
                    throw InternalServerError.schedulerFailError(e.getMessage());
                }
            }
        }
    }

    // 날씨 데이터 처리
    private void processRegionWeather(String region, String code) {
        String dateFormatMid = getFixedTime(6, 0).format(FULL_DATE_TIME_FORMATTER);
        String dateFormatST = getYesterday().format(DATE_FORMATTER);

        String[] xy = RegionMapper.getWeatherXYRegion(region);

        String responseMid = weatherApiService.getWeatherForecastMid(code, dateFormatMid);
        String responseST = weatherApiService.getWeatherForecastST(xy, dateFormatST);

        // 단기
        List<String> weatherCodes = parseShortTermWeather(responseST);

        // 중기
        JsonNode itemsNodeMid = parseJsonNode(responseMid, "response", "body", "items", "item");

        if (itemsNodeMid.isArray() && itemsNodeMid.size() > 0) {
            // 저장 전 매핑
            ObjectNode weatherSummary = createWeatherSummary(itemsNodeMid, weatherCodes);

            // redis에 저장
            saveToRedis(region, weatherSummary);
        } else {
            throw NotFoundException.entityNotFound("중기 예보 데이터");
        }
    }

    private JsonNode parseJsonNode(String jsonString, String... paths) { // 가변인자
        try {
            JsonNode node = objectMapper.readTree(jsonString);
            for (String path : paths) {
                node = node.path(path);
            }
            return node;
        } catch (Exception e) {
            throw InternalServerError.weatherApiError("JSON 데이터 파싱 중 오류 발생: " + e.getMessage());
        }
    }

    // 단기 데이터 파싱
    private List<String> parseShortTermWeather(String response) {
        JsonNode itemsNode = parseJsonNode(response, "response", "body", "items", "item");
        List<String> weatherCodes = new ArrayList<>();
        List<LocalDate> targetDates = List.of(LocalDate.now(), LocalDate.now().plusDays(1), LocalDate.now().plusDays(2));

        for (LocalDate targetDate : targetDates) {
            String targetDateString = targetDate.format(DATE_FORMATTER);
            weatherCodes.add(findWeatherCode(itemsNode, targetDateString));
        }
        return weatherCodes;
    }

    // 단기 데이터 PTY SKY 수집
    private String findWeatherCode(JsonNode itemsNode, String targetDate) {
        String ptyValue = null;
        String skyValue = null;

        for (JsonNode item : itemsNode) {
            String category = item.path("category").asText();
            String fcstDate = item.path("fcstDate").asText();
            String fcstTime = item.path("fcstTime").asText();

            // 15시 기준 데이터 반환
            if (fcstDate.equals(targetDate) && "1500".equals(fcstTime)) {
                // 강수 없음이면 SKY 코드, 강수 있으면 PTY 코드
                if ("PTY".equals(category)) ptyValue = item.path("fcstValue").asText();
                if ("SKY".equals(category)) skyValue = item.path("fcstValue").asText();
            }
        }

        // 날씨 코드 텍스트로 매핑
        if (ptyValue != null && !"0".equals(ptyValue)) return WeatherMapper.getWeatherPtyCode(ptyValue);
        if (skyValue != null) return WeatherMapper.getWeatherSkyCode(skyValue);
        return "";
    }

    // 저장 전 매핑
    private ObjectNode createWeatherSummary(JsonNode itemsNodeMid, List<String> weatherCodes) {
        JsonNode firstItem = itemsNodeMid.get(0);
        ObjectNode summary = objectMapper.createObjectNode();

        // Weather codes 처리
        for (int i = 0; i < weatherCodes.size(); i++) {
            String weather = weatherCodes.get(i);
            summary.put("day" + (i + 1), extractLastWord(weather));
        }

        // JSON 노드에서 날씨 데이터 처리
        summary.put("day4", extractLastWord(firstItem.path("wf4Pm").asText()));
        summary.put("day5", extractLastWord(firstItem.path("wf5Pm").asText()));
        summary.put("day6", extractLastWord(firstItem.path("wf6Pm").asText()));
        summary.put("day7", extractLastWord(firstItem.path("wf7Pm").asText()));
        summary.put("day8", extractLastWord(firstItem.path("wf8").asText()));
        summary.put("day9", extractLastWord(firstItem.path("wf9").asText()));
        summary.put("day10", extractLastWord(firstItem.path("wf10").asText()));

        return summary;
    }

    // 마지막 단어 추출 메서드
    private String extractLastWord(String weather) {
        if (weather == null || weather.isEmpty()) {
            return weather; // null 또는 빈 문자열 처리
        }
        String[] words = weather.split(" ");
        return words[words.length - 1];
    }


    // 레디스에 저장
    private void saveToRedis(String region, ObjectNode weatherSummary) {
        String key = WEATHER_KEY + RegionMapper.getRegionEng(region);
        redisTemplate.opsForValue().setIfAbsent(key, weatherSummary.toString(), Duration.ofHours(24));
        log.info("날씨 정보 redis에 저장 완료!");
    }

    private LocalDateTime getFixedTime(int hour, int minute) {
        return LocalDateTime.now().withHour(hour).withMinute(minute);
    }

    private LocalDate getYesterday() {
        return LocalDate.now().minusDays(1);
    }
}
