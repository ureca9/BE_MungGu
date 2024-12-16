package com.meong9.backend.global.banword.manager;

import com.meong9.backend.global.banword.domain.Word;
import com.meong9.backend.global.banword.util.WordUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BanWordManager extends AbstractWordManager {
    public BanWordManager(@Qualifier("ban") WordUtil wordUtil) {
        super(wordUtil);
    }

    public final List<Word> filter(String word) {
        return wordUtil.search(word);
    }
}
