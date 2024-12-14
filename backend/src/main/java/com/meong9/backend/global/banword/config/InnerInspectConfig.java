package com.meong9.backend.global.banword.config;

import com.meong9.backend.global.banword.util.wordutil.ExceptWordUtil;
import com.meong9.backend.global.banword.factory.WordFactoryBuilder;
import com.meong9.backend.global.banword.util.wordutil.BadWordUtil;
import com.meong9.backend.global.banword.wordloader.WordLoader;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InnerInspectConfig {
    private final WordFactoryBuilder<BadWordUtil> banWordFactory;
    private final WordFactoryBuilder<ExceptWordUtil> exceptWordFactory;
    private final WordLoader wordLoader;
    private InspectConfig inspectConfig;

    @Autowired
    public InnerInspectConfig(WordFactoryBuilder<BadWordUtil> banWordFactory, WordFactoryBuilder<ExceptWordUtil> exceptWordFactory, WordLoader wordLoader) {
        this.banWordFactory = banWordFactory;
        this.exceptWordFactory = exceptWordFactory;
        this.wordLoader = wordLoader;
    }

    @Autowired(required = false)
    public void setInspectConfig(InspectConfig inspectConfig) {
        this.inspectConfig = inspectConfig;
    }

    @PostConstruct
    private void onApplicationReady() {
        if (inspectConfig != null) {
            inspectConfig.addBanWords(banWordFactory);
            inspectConfig.addExceptWords(exceptWordFactory);
        }

        banWordFactory.add(wordLoader.readBanWords());
        exceptWordFactory.add(wordLoader.readExceptWords());
    }

    public BadWordUtil getBanWordUtil() {
        return banWordFactory.build();
    }

    public ExceptWordUtil getExceptWordUtil() {
        return exceptWordFactory.build();
    }

}