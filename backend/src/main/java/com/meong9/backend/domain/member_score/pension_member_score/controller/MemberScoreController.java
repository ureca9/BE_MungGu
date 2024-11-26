package com.meong9.backend.domain.member_score.pension_member_score.controller;

import com.meong9.backend.domain.member_score.pension_member_score.service.PensionMemberScoreService;
import com.meong9.backend.domain.member_score.place_member_score.service.PlaceMemberScoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MemberScoreController {
    private final PensionMemberScoreService pensionMemberScoreService;
    private final PlaceMemberScoreService placeMemberScoreService;

    @GetMapping("/score")
    public void scoreSetting(){
        pensionMemberScoreService.initializeScores();
        placeMemberScoreService.initializeScores();
    }
}
