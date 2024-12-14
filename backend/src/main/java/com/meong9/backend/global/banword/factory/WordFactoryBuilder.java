package com.meong9.backend.global.banword.factory;

import com.meong9.backend.global.banword.util.wordutil.AbstractWordUtil;

public interface WordFactoryBuilder<T extends AbstractWordUtil> extends WordFactory {
    T build();
}