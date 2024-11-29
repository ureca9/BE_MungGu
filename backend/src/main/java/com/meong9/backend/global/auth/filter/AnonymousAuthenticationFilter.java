package com.meong9.backend.global.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.log.LogMessage;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Slf4j(topic = "AnonymousAuthenticationFilter")
public class AnonymousAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            Authentication authentication = createAuthentication((HttpServletRequest) req);
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            if (this.logger.isTraceEnabled()) {
                this.logger.trace(LogMessage.of(() -> "Set SecurityContextHolder to"
                        + SecurityContextHolder.getContext().getAuthentication()));
            } else {
                this.logger.debug("Set SecurityContextHolder to anonymous SecurityContext");
            }
        } else {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace(LogMessage.of(() -> "Did not set SecurityContextHolder since already authenticated"
                        + SecurityContextHolder.getContext().getAuthentication()));
            }
        }
        filterChain.doFilter(req, res);
    }

    private Authentication createAuthentication(HttpServletRequest request) {
        // 익명 사용자 principal 설정
        String anonymousPrincipal = "anonymousUser";

        // 익명 사용자 권한 설정
        List<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS");

        // 익명 사용자 Authentication 객체 생성
        return new AnonymousAuthenticationToken("anonymousKey", anonymousPrincipal, authorities);
    }
}
