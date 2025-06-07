package com.meong9.backend.global.banword.factory;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.global.auth.entity.MemberDetails;
import com.meong9.backend.global.banword.manager.ExceptWordManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Component;

@Component
public class ExceptWordFactory extends AbstractWordFactory<ExceptWordManager> {
    public ExceptWordFactory(ExceptWordManager exceptWordManager) {
        super(exceptWordManager);
    }
}