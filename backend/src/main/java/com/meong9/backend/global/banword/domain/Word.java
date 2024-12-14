package com.meong9.backend.global.banword.domain;

public record Word(String word, int startIndex, int endIndex) {

    public boolean isInclude(Word word) {
        return word.startIndex <= startIndex && word.endIndex >= endIndex;
    }

}
