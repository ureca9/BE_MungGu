package com.meong9.backend.global.auth.entity;

import com.meong9.backend.domain.member.entity.Member;
import com.meong9.backend.global.utils.RoleCodeMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

public record MemberDetails (Member member) implements UserDetails {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String commonCode = member.getRoleCode(); // ex) "010"
        String role = RoleCodeMapper.getRole(commonCode); // ex) MEMBER
        return Collections.singleton(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return member().getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return member.getBlackList() == null || // 블랙리스트에 등록된 적도 없을 때
                member.getBlackList().getLockedUntil() == null || // 블랙 리스트에 등록되었지만 기간이 등록되어 있지 않을 때
                member.getBlackList().getLockedUntil().isBefore(LocalDateTime.now()); // 블랙 리스트 기간이 지났을 때
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
