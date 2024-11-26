package com.meong9.backend.global.auth.config;

import com.meong9.backend.global.auth.filter.JwtAuthenticationFilter;
import com.meong9.backend.global.auth.service.MemberDetailsService;
import com.meong9.backend.global.auth.utils.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtProvider jwtProvider;
    private final MemberDetailsService memberDetailsService;
    private final AuthenticationConfiguration authenticationConfiguration;

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000", "http://localhost:8080"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT", "PATCH", "DELETE","OPTIONS"));
        configuration.setMaxAge(60L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 필터링에서 제외할 요청들
        final RequestMatcher ignoredRequests = new OrRequestMatcher(
                List.of(new AntPathRequestMatcher("/api/v1/auth/callback/kakao", HttpMethod.POST.name()),
                        new AntPathRequestMatcher("/api/v1/auth/token", HttpMethod.POST.name()),
                        new AntPathRequestMatcher("/api/v1/members/check", HttpMethod.GET.name()),
                        new AntPathRequestMatcher("/api/v1/searches/places", HttpMethod.GET.name()),
                        new AntPathRequestMatcher("/api/v1/searches/pensions", HttpMethod.GET.name()),
                        new AntPathRequestMatcher("/api/v1/spots/rankings", HttpMethod.GET.name()),
                        new AntPathRequestMatcher("/api/v1/spots/recommendations", HttpMethod.GET.name()),
                        new AntPathRequestMatcher("/api/v1/pensions/{pensionId}", HttpMethod.GET.name()),
                        new AntPathRequestMatcher("/api/v1/pensions/{placeId}", HttpMethod.GET.name()),
                        new AntPathRequestMatcher("/api/v1/pensions/{placeId}/reviews", HttpMethod.GET.name()),
                        new AntPathRequestMatcher("/", HttpMethod.GET.name()),
                        new AntPathRequestMatcher("/actuator/health", HttpMethod.GET.name())
                ));

        // 요청별 권한 관리
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(ignoredRequests).permitAll()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll() // 정적 리소스 허용
                .requestMatchers("/index.html", "/favicon.ico").permitAll()
                .requestMatchers(HttpMethod.GET, "/ping", "/error", "/actuator/health").permitAll() // 헬스 체크 허용
                .anyRequest().authenticated() // 나머지 요청은 MEMBER 역할 필요
        );

        // CSRF 비활성화 및 CORS 설정
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // 세션 비활성화 (JWT 사용)
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 로그인 폼 비활성화
        http.formLogin(AbstractHttpConfigurer::disable);

        http.addFilterBefore(new JwtAuthenticationFilter(jwtProvider, memberDetailsService, ignoredRequests),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
