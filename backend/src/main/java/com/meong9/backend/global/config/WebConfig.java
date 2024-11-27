package com.meong9.backend.global.config;

import com.meong9.backend.global.annotation.member.CurrentMemberArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final CurrentMemberArgumentResolver currentMemberArgumentResolver;

    public WebConfig(CurrentMemberArgumentResolver currentMemberArgumentResolver) {
        this.currentMemberArgumentResolver = currentMemberArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentMemberArgumentResolver);
    }
}
