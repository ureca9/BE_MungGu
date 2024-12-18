package com.meong9.backend.global.blacklist.scheduler;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.global.blacklist.entity.BlackList;
import com.meong9.backend.global.blacklist.repository.BlackListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class BlackListScheduler {

    private final BlackListRepository blackListRepository;

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정 실행
    public void removeExpiredBlackLists() {
        List<BlackList> expiredBlackLists = blackListRepository.findAllByLockedUntilBefore(LocalDateTime.now());

        if (!expiredBlackLists.isEmpty()) {
            // BlackList 삭제 전에 Member에서 연결 해제
            expiredBlackLists.forEach(blackList -> {
                Member member = blackList.getMember();
                if (member != null) {
                    member.setBlackList(null);
                }
            });

            blackListRepository.deleteAllInBatch(expiredBlackLists);
        }
    }
}
