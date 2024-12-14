package com.meong9.backend.global.banword.factory;

import com.meong9.backend.global.banword.service.AbstractWordManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractWordFactory<T extends AbstractWordManager> implements WordFactoryBuilder<T> {

    private final T wordUtil;
    private final Set<String> distinctWordSet = new HashSet<>();

    public AbstractWordFactory(T wordUtil) {
        this.wordUtil = wordUtil;
    }

    @Override
    public WordFactory add(List<String> words) {
        distinctWordSet.addAll(words);
        return this;
    }

    @Override
    public T build() {
        distinctWordSet.forEach(wordUtil::addWord);
        wordUtil.build();
        return wordUtil;
    }
}
