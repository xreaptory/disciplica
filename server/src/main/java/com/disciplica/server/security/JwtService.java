package com.disciplica.server.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.disciplica.server.config.JwtProperties;

/**
 * Dienst zum Erstellen von Access-Tokens (JWT) für angemeldete Benutzer.
 */
@Service
public class JwtService {
    private final JwtProperties properties;
    private final JwtEncoder encoder;

    /**
     * Erzeugt den Dienst mit der JWT-Konfiguration und dem Token-Encoder.
     *
     * @param properties die JWT-Konfiguration (Aussteller, Gültigkeitsdauer)
     * @param encoder    die Komponente zum Signieren der Tokens
     */
    public JwtService(JwtProperties properties, JwtEncoder encoder) {
        this.properties = properties;
        this.encoder = encoder;
    }

    /**
     * Erstellt ein signiertes Access-Token für den angegebenen Benutzer. Das
     * Token enthält die Benutzer-Kennung als Subjekt sowie den Benutzernamen
     * und läuft nach der konfigurierten Dauer ab.
     *
     * @param userId   die Kennung des Benutzers
     * @param username der Benutzername, der ins Token geschrieben wird
     * @return das signierte JWT als Zeichenkette
     */
    public String createAccessToken(UUID userId, String username) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .issuedAt(now)
                .expiresAt(now.plus(properties.accessTokenMinutes(), ChronoUnit.MINUTES))
                .subject(userId.toString())
                .claim("username", username)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
