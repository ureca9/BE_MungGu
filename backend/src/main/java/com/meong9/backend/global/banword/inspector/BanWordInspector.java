package com.meong9.backend.global.banword.inspector;

import com.meong9.backend.global.banword.domain.Word;
import com.meong9.backend.global.banword.config.InspectorConfig;
import com.meong9.backend.global.banword.manager.ExceptWordManager;
import com.meong9.backend.global.banword.manager.BanWordManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BanWordInspector {
    private final BanWordManager banWordManager;
    private final ExceptWordManager exceptWordManager;
    private static final String REMOVE_PATTERN = "[\\p{N}\\s\\u3164\\p{L}&&[^ㄱ-ㅎ가-힣ㅏ-ㅣa-zA-Z]]";

    @Autowired
    public BanWordInspector(InspectorConfig config) {
        banWordManager = config.getBanWordUtil();
        exceptWordManager = config.getExceptWordUtil();
    }

    private List<Word> executeBanWord(String word) {
        return banWordManager.filter(word);
    }

    private List<Word> executeExceptWord(String word, List<Word> beforeWords) {
        return exceptWordManager.filter(word, beforeWords);
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
        /**
         입력: 과징금 크악 씨  이  빨
         문자 제거: 과징금크악씨이빨

         기대 결과: 과징금 크악 멍멍
         실제 결과: 과징금크악멍멍

         **/
        for (int i = data.size() - 1; i >= 0; i--) {
            sb.replace(data.get(i).startIndex(), data.get(i).endIndex(), replace);
        }
        return sb.toString();
    }
}