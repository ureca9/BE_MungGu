package com.meong9.backend.global.banword.config;

import com.meong9.backend.global.banword.service.AhoCorasickWordUtil;
import com.meong9.backend.global.banword.service.WordUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WordUtilConfig {
    @Bean("ban")
    WordUtil banWordUtil() {
        return new AhoCorasickWordUtil();
    }

    @Bean("except")
    WordUtil exceptWordUtil() {
        return new AhoCorasickWordUtil();
    }
}
