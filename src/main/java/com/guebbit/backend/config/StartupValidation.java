package com.guebbit.backend.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class StartupValidation {
    private final AppProperties appProperties;

    public StartupValidation(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @PostConstruct
    void validate() {
        if (appProperties.security().accessTokenSecret().length() < 32 || appProperties.security().refreshTokenSecret().length() < 32) {
            throw new IllegalStateException("JWT secrets must be at least 32 characters");
        }
    }
}
