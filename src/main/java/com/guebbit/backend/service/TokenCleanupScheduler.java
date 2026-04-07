package com.guebbit.backend.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TokenCleanupScheduler {
    private final AuthService authService;

    public TokenCleanupScheduler(AuthService authService) {
        this.authService = authService;
    }

    @Scheduled(fixedDelayString = "${app.token-cleanup-interval-minutes:30}m")
    public void cleanup() {
        authService.deleteExpiredTokens();
    }
}
