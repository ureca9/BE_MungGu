package com.meong9.backend.global.banword.util.wordutil;

import com.meong9.backend.global.banword.domain.Word;
import com.meong9.backend.global.banword.util.WordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class ExceptWordUtil extends AbstractWordUtil {

    public ExceptWordUtil(@Qualifier("except") WordUtil wordUtil) {
        super(wordUtil);
    }

    public final List<Word> filter(String newWord, List<Word> banWords) {
        log.info("banword 크기: {}",banWords.size());
        return (banWords.isEmpty()) ? List.of() : exceptFilter(newWord, banWords);
    }

    private List<Word> exceptFilter(String newWord, List<Word> banWords) {
        List<Word> exceptWords = wordUtil.search(newWord);
        log.info("newWord: {}",newWord);
        for (Word word : exceptWords) {
            log.info("exceptWord: {}",word);
        }
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