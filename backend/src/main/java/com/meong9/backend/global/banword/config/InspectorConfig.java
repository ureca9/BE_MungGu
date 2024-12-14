package com.meong9.backend.global.banword.config;

import com.meong9.backend.global.banword.service.ExceptWordManager;
import com.meong9.backend.global.banword.factory.WordFactoryBuilder;
import com.meong9.backend.global.banword.service.BanWordManager;
import com.meong9.backend.global.banword.wordloader.WordLoader;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InspectorConfig {
    private final WordFactoryBuilder<BanWordManager> banWordFactory;
    private final WordFactoryBuilder<ExceptWordManager> exceptWordFactory;
    private final WordLoader wordLoader;

    @Autowired
    public InspectorConfig(WordFactoryBuilder<BanWordManager> banWordFactory, WordFactoryBuilder<ExceptWordManager> exceptWordFactory, WordLoader wordLoader) {
        this.banWordFactory = banWordFactory;
        this.exceptWordFactory = exceptWordFactory;
        this.wordLoader = wordLoader;
    }

    @PostConstruct
    private void onApplicationReady() {
        banWordFactory.add(wordLoader.readBanWords());
        exceptWordFactory.add(wordLoader.readExceptWords());
    }

    public BanWordManager getBanWordUtil() {
        return banWordFactory.build();
    }

    public ExceptWordManager getExceptWordUtil() {
        return exceptWordFactory.build();
    }

}