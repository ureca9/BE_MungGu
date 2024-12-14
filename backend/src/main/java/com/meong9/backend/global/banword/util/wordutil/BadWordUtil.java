package com.meong9.backend.global.banword.util.wordutil;

import com.meong9.backend.global.banword.domain.Word;
import com.meong9.backend.global.banword.util.WordUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BadWordUtil extends AbstractWordUtil{
    public BadWordUtil(@Qualifier("bad") WordUtil wordUtil) {
        super(wordUtil);
    }

    public final List<Word> filter(String word) {
        return wordUtil.search(word);
    }

}
