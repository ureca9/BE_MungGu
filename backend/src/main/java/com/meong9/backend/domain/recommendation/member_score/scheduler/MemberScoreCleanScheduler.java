package com.meong9.backend.domain.recommendation.member_score.scheduler;

import com.meong9.backend.domain.recommendation.member_score.pension_member_score.repository.PensionMemberScoreRepository;
import com.meong9.backend.domain.recommendation.member_score.place_member_score.repository.PlaceMemberScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class MemberScoreCleanScheduler {
    private final PensionMemberScoreRepository pensionMemberScoreRepository;
    private final PlaceMemberScoreRepository placeMemberScoreRepository;

    @Scheduled(cron = "0 0 3 * * ?") // 매일 새벽 3시에 실행
    public void cleanupOldScores() {
        LocalDateTime threshold = LocalDateTime.now().minusMonths(36); // 3년 기준
        pensionMemberScoreRepository.deleteByLastUpdatedAtBefore(threshold);
        placeMemberScoreRepository.deleteByLastUpdatedAtBefore(threshold);
    }
}
