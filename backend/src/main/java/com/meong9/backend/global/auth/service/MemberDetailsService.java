package com.meong9.backend.global.auth.service;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.domain.member.repository.MemberRepository;
import com.meong9.backend.global.auth.entity.MemberDetails;
import com.meong9.backend.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public MemberDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> NotFoundException.entityNotFound("멤버"));
        return new MemberDetails(member);

    }
}
