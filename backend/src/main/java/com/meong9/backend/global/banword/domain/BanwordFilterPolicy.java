package com.meong9.backend.global.banword.domain;

public enum BanwordFilterPolicy {
    NUMBERS("[\\p{N}]"),
    WHITESPACES("[\\s\\u3164]"),
    FOREIGNLANGUAGES("[\\p{L}&&[^ㄱ-ㅎ가-힣ㅏ-ㅣa-zA-Z]]");

    private final String regex;

    BanwordFilterPolicy(String regex) {
        this.regex = regex;
    }
}
