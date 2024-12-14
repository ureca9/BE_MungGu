package com.meong9.backend.global.banword.util.wordutil;

import com.meong9.backend.global.banword.util.WordUtil;

public class AbstractWordUtil {
    protected final WordUtil wordUtil;

    public AbstractWordUtil(WordUtil wordUtil) {
        this.wordUtil = wordUtil;
    }

    public void addWord(String word) {
        wordUtil.addWord(word);
    }

    public void build() {
        wordUtil.build();
    }
}
