package com.meong9.backend.global.banword.config;

import com.meong9.backend.global.banword.util.AhoCorasickWordUtil;
import com.meong9.backend.global.banword.util.WordUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WordUtilConfig {
    @Bean("bad")
    WordUtil badWordUtil() {
        return new AhoCorasickWordUtil();
    }
    @Bean("except")
    WordUtil exceptWordUtil() {
        return new AhoCorasickWordUtil();
    }
}
