package com.meong9.backend.global.banword.config;

import com.meong9.backend.global.banword.factory.WordFactory;

public interface InspectConfig {
    void addBanWords(WordFactory factory);
    void addExceptWords(WordFactory factory);
}
