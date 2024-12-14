package com.meong9.backend.global.banword.factory;

import com.meong9.backend.global.banword.manager.AbstractWordManager;

public interface WordFactoryBuilder<T extends AbstractWordManager> extends WordFactory {
    T build();
}