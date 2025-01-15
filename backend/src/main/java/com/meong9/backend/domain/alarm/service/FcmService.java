package com.meong9.backend.domain.alarm.service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.meong9.backend.domain.alarm.FcmMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {
    private final String API_URL = "https://fcm.googleapis.com/v1/projects/mungtivity/messages:send";
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final String FCM_KEY = "fcm_token:";
    private final String FCM_TITLE = "멍티비티";
    private final String FCM_BODY = "우리 댕댕이와 여기 어때요?";
    private final int BATCH_SIZE = 1000; // 배치 크기 설정
    private final int MAX_RETRY = 3;
    private final long RETRY_DELAY_MS = 1000; // 1초 대기

    // 매일 오후 3시에 FCM 알람
    @Scheduled(cron = "0 0 15 * * ?") // 매일 오후 3시에 실행
//    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void sendAnniversaryNotifications() {
        Set<String> tokenKeys = getAllTokensUsingScan();
        if (tokenKeys.isEmpty()) {
            log.info("FCM 토큰을 찾을 수 없습니다. (비어있음)");
            return;
        }

        // 배치 처리로 변경
        List<String> tokenList = new ArrayList<>(tokenKeys);
        for (int i = 0; i < tokenList.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, tokenList.size());
            List<String> batch = tokenList.subList(i, end);

            batch.parallelStream().forEach(key -> {
                String token = getToken(key);
                if (token != null) {
                    sendMessageWithRetry(key, token, FCM_TITLE, FCM_BODY, MAX_RETRY); // 알림
                }
            });
        }
    }

    // 토큰 저장
    public void saveToken(Long key, String token, long ttlInSeconds) {
        redisTemplate.opsForValue().set(FCM_KEY + key, token, ttlInSeconds, TimeUnit.SECONDS);
    }

    // 알림 전송 로직 - 실패 시 재시도
    private void sendMessageWithRetry(String key, String token, String title, String body, int remainingRetries) {
        try {
            sendMessageTo(token, title, body);
        } catch (IOException e) {
            if (remainingRetries > 0) {
                log.warn("토큰: {}로 FCM 알림 전송 실패. 재시도 중... (남은 시도 횟수: {})", key, remainingRetries);

                try {
                    Thread.sleep(RETRY_DELAY_MS);
                    sendMessageWithRetry(key, token, title, body, remainingRetries - 1);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("토큰: {}의 재시도 중 인터럽트 발생", key, ie);
                }
            } else {
                log.error("FCM 알림 전송 최종 실패. 토큰: {}", key, e);

                // 토큰이 만료되었거나 유효하지 않은 경우 삭제
                if (isInvalidTokenError(e)) {
                    deleteToken(key);
                    log.info("유효하지 않은 토큰 삭제: {}", key);
                }
            }
        }
    }

    // 비동기로 알림 메세지 전송
    @Async
    public void sendMessageTo(String targetToken, String title, String body) throws IOException {
        String message = makeMessage(targetToken, title, body);

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(message,
                MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + getAccessToken())
                .addHeader(HttpHeaders.CONTENT_TYPE, "application/json; UTF-8")
                .build();

        Response response = client.newCall(request).execute();

        System.out.println(response.body().string());
    }



    private String makeMessage(String targetToken, String title, String body) throws JsonProcessingException {
        FcmMessage fcmMessage = FcmMessage.builder()
                .message(FcmMessage.Message.builder()
                        .token(targetToken)
                        .notification(FcmMessage.Notification.builder()
                                .title(title)
                                .body(body)
                                .image(null)
                                .build()
                        ).build()).validateOnly(false).build();

        return objectMapper.writeValueAsString(fcmMessage);
    }

    // SCAN을 사용한 토큰 조회
    private Set<String> getAllTokensUsingScan() {
        Set<String> tokens = new HashSet<>();
        int batchSize = 1000;

        ScanOptions scanOptions = ScanOptions.scanOptions()
                .match(FCM_KEY + "*")
                .count(batchSize)
                .build();

        Cursor<String> cursor = redisTemplate.scan(scanOptions);
        while (cursor.hasNext()) {
            tokens.add(cursor.next());
        }
        return tokens;
    }

    // fcm access token 발급
    private String getAccessToken() throws IOException {
        String firebaseConfigPath = "static/firebase_service_key.json";

        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource(firebaseConfigPath).getInputStream())
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }

    // FCM 응답에서 토큰 관련 에러 확인
    private boolean isInvalidTokenError(IOException e) {
        // 토큰 형식 잘못됐거나, 앱 삭제, 토큰 만료
        return e.getMessage().contains("InvalidRegistration") ||
                e.getMessage().contains("NotRegistered") ||
                e.getMessage().contains("InvalidToken");
    }

    // 토큰 조회
    private String getToken(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }

    // 토큰 삭제
    private void deleteToken(String key) {
        redisTemplate.delete(key);
    }
}
