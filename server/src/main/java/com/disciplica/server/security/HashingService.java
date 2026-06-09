package com.disciplica.server.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HexFormat;

import org.springframework.stereotype.Service;

/**
 * Dienst für kryptografische Hilfsfunktionen: Erzeugen zufälliger Tokens und
 * Bilden von SHA-256-Hashwerten.
 */
@Service
public class HashingService {
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Erzeugt ein neues, zufälliges, undurchsichtiges Token (z.&nbsp;B. für
     * Refresh-Tokens).
     *
     * @return ein URL-sicheres, zufälliges Token
     */
    public String newOpaqueToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Bildet den SHA-256-Hashwert einer Zeichenkette als Hex-Text.
     *
     * @param value der zu hashende Wert
     * @return der Hashwert in hexadezimaler Schreibweise
     * @throws IllegalStateException wenn der SHA-256-Algorithmus nicht
     *                               verfügbar ist
     */
    public String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}
