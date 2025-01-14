package com.meong9.backend.global.banword.inspector;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.member.service.MemberService;
import com.meong9.backend.global.banword.config.InspectorConfig;
import com.meong9.backend.global.banword.domain.Word;
import com.meong9.backend.global.banword.manager.BanWordManager;
import com.meong9.backend.global.banword.manager.ExceptWordManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class BanWordInspector {
    private final BanWordManager banWordManager;
    private final ExceptWordManager exceptWordManager;
    private final MemberService memberService;

    @Autowired
    public BanWordInspector(InspectorConfig config, MemberService memberService) {
        banWordManager = config.getBanWordUtil();
        exceptWordManager = config.getExceptWordUtil();
        this.memberService = memberService;
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

    public String mask(String word, Member member) {
        return mask(word,"어머", member);
    }

    public String mask(String word, String replace, Member member) {
        StringBuilder sb = new StringBuilder(word);
        List<Word> data = inspect(word);

        if (!data.isEmpty()) {
            memberService.handleBadPost(member);
        }

        for (int i = data.size() - 1; i >= 0; i--) {
            sb.replace(data.get(i).startIndex(), data.get(i).endIndex(), replace);
        }

        return sb.toString();
    }
}