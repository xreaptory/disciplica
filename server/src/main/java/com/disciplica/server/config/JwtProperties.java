package com.disciplica.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "disciplica.jwt")
public record JwtProperties(
        String issuer,
        String secret,
        long accessTokenMinutes,
        long refreshTokenDays
) {
}
