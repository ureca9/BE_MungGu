package com.meong9.backend.global.auth.filter;

import com.meong9.backend.global.auth.utils.JwtProvider;
import com.meong9.backend.global.exception.AuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j(topic = "JwtAuthorizationFilter")
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsService detailsService;
    private final RequestMatcher ignoredRequests;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain) {
        try {
            // 1. RequestMatcher로 매칭되는 요청에 대해 필터 건너뛰기
            if (ignoredRequests.matches(req)) {
                filterChain.doFilter(req, res);
                return;
            }

            // 2. Refresh Token 요청 처리
            if (req.getRequestURI().equals("/api/v1/auth/token")) {
                handleRefreshToken(req, res, filterChain);
                return;
            }

            // 3. Access Token 인증 처리
            String tokenValue = jwtProvider.getTokenFromRequest(req, JwtProvider.AUTHORIZATION_HEADER);
            if (!StringUtils.hasText(tokenValue)) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            jwtProvider.validateToken(tokenValue);

            // 4. 사용자 정보 추출 및 인증 설정
            String username = jwtProvider.getSubjectFromToken(tokenValue);
            UserDetails userDetails = detailsService.loadUserByUsername(username);

            validateUserRole(userDetails, jwtProvider.getRoleFromToken(tokenValue));
            setAuthentication(userDetails);

            filterChain.doFilter(req, res);

        } catch (Exception e) {
            log.error("인증에 실패했습니다: {}", e.getMessage());
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    private void handleRefreshToken(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain filterChain) throws IOException, ServletException {
        String refreshToken = jwtProvider.getTokenFromRequest(req, JwtProvider.REFRESH_TOKEN_HEADER);
        if (!StringUtils.hasText(refreshToken)) {
            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        filterChain.doFilter(req, res);
    }

    private void validateUserRole(UserDetails userDetails, String role) {
        boolean hasRole = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals(role));
        if (!hasRole) {
            throw AuthenticationException.noUserRole();
        }
    }

    private void setAuthentication(UserDetails userDetails) {
        Authentication authentication = createAuthentication(userDetails);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Authentication createAuthentication(UserDetails userDetails) {
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
