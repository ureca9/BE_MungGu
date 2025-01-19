package com.meong9.backend.domain.alarm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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
        private Notification notification;
        private String token;
        private WebPush webpush;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    public static class Notification {
        private String title;
        private String body;
        private String image;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    public static class WebPush {
        private WebpushFcmOptions fcmOptions;
    }

    @Builder
    @AllArgsConstructor
    @Getter
    public static class WebpushFcmOptions {
        private String link;
    }
}