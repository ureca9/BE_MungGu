package com.meong9.backend.global.banword.manager;

import com.meong9.backend.global.banword.util.WordUtil;

public class AbstractWordManager {
    protected final WordUtil wordUtil;

    public AbstractWordManager(WordUtil wordUtil) {
        this.wordUtil = wordUtil;
    }

    public void addWord(String word) {
        wordUtil.addWord(word);
    }

    public void build() {
        wordUtil.build();
    }
}
