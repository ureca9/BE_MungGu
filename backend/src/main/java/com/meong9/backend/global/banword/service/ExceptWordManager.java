package com.meong9.backend.global.banword.service;

import com.meong9.backend.global.banword.domain.Word;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ExceptWordManager extends AbstractWordManager {
    public ExceptWordManager(@Qualifier("except") WordUtil wordUtil) {
        super(wordUtil);
    }

    public final List<Word> filter(String newWord, List<Word> banWords) {
        return (banWords.isEmpty()) ? List.of() : exceptFilter(newWord, banWords);
    }

    private List<Word> exceptFilter(String newWord, List<Word> banWords) {
        List<Word> exceptWords = wordUtil.search(newWord);

        if (exceptWords.isEmpty()) return banWords;

        List<Word> newWords = new ArrayList<>();

        a:for (Word banWord : banWords) {
            for (Word exceptWord : exceptWords) {
                if (banWord.isInclude(exceptWord)) continue a;
            }
            newWords.add(banWord);
        }
        return newWords;
    }
}