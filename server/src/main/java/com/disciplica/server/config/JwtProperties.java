package com.disciplica.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Konfigurationswerte für die Erzeugung und Prüfung von JWT-Tokens.
 * <p>
 * Die Werte werden aus der Konfiguration mit dem Präfix
 * {@code disciplica.jwt} eingelesen.
 *
 * @param issuer             Aussteller-Kennung, die in jedes Token
 *                           geschrieben wird
 * @param secret             geheimer Schlüssel zum Signieren der Tokens
 * @param accessTokenMinutes Gültigkeitsdauer des Access-Tokens in Minuten
 * @param refreshTokenDays   Gültigkeitsdauer des Refresh-Tokens in Tagen
 */
@ConfigurationProperties(prefix = "disciplica.jwt")
public record JwtProperties(
        String issuer,
        String secret,
        long accessTokenMinutes,
        long refreshTokenDays
) {
}
