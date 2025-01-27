package com.meong9.backend.domain.alarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.Map;

@Builder
@AllArgsConstructor
@Getter
public class FcmMessage {
    private boolean validateOnly;
    private Message message;

    @Builder
    @AllArgsConstructor
    @Getter
    public static class Message {
        private Map<String, String> data;  // data 필드를 추가하여 알림 데이터를 Map 형태로 전달
        private String token;              // FCM 토큰
    }
}
