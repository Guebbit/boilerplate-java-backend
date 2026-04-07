package com.guebbit.backend.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app")
@Validated
public record AppProperties(
        Security security,
        Tokens tokens,
        @Min(1) int rateLimitPerMinute,
        @Min(1) int tokenCleanupIntervalMinutes
) {
    public record Security(
            @NotBlank String accessTokenSecret,
            @NotBlank String refreshTokenSecret,
            @NotBlank String issuer
    ) {}

    public record Tokens(
            @Min(60) int accessTokenSeconds,
            @Min(300) int refreshTokenSeconds
    ) {}
}
