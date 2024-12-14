package com.meong9.backend.global.banword.inspector;

import com.meong9.backend.global.banword.domain.Word;
import com.meong9.backend.global.banword.config.InnerInspectConfig;
import com.meong9.backend.global.banword.util.wordutil.ExceptWordUtil;
import com.meong9.backend.global.banword.util.wordutil.BadWordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class BadWordInspector {

    private final BadWordUtil badWordUtil;
    private final ExceptWordUtil exceptWordUtil;


    @Autowired
    public BadWordInspector(InnerInspectConfig config) {
        badWordUtil = config.getBanWordUtil();
        exceptWordUtil = config.getExceptWordUtil();
    }

    private List<Word> executeBanWord(String word) {
        return badWordUtil.filter(word);
    }

    private List<Word> executeExceptWord(String word, List<Word> beforeWords) {
        return exceptWordUtil.filter(word, beforeWords);
    }

    public List<Word> inspect(String word) {
        return executeExceptWord(word, executeBanWord(word));
    }

    public String mask(String word) {
        return mask(word,"어머");
    }

    public String mask(String word, String replace) {
        StringBuilder sb = new StringBuilder(word);
        List<Word> data = inspect(word);
        log.info("data 사이즈: {}",data.size());

        for (int i = data.size() - 1; i >= 0; i--) {
            sb.replace(data.get(i).startIndex(), data.get(i).endIndex(), replace);
        }
        return sb.toString();
    }
}