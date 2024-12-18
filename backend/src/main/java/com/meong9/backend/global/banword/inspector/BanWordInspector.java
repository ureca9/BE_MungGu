package com.meong9.backend.global.banword.inspector;

import com.meong9.backend.global.banword.domain.Word;
import com.meong9.backend.global.banword.config.InspectorConfig;
import com.meong9.backend.global.banword.manager.ExceptWordManager;
import com.meong9.backend.global.banword.manager.BanWordManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
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

        for (int i = data.size() - 1; i >= 0; i--) {
            sb.replace(data.get(i).startIndex(), data.get(i).endIndex(), replace);
        }

        return sb.toString();
    }
}