package com.meong9.backend.global.banword.util;

import com.meong9.backend.global.banword.domain.Word;

import java.util.List;

public interface WordUtil {
    void addWord(String word);
    void build();
    List<Word> search(String word);
}
