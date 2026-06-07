package com.disciplica.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "disciplica.google")
public record GoogleProperties(
        String clientId,
        String clientSecret,
        String publicBaseUrl
) {
}
