package com.disciplica.server.security;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Hilfskomponente, um aus dem Anmeldekontext die Kennung des aktuell
 * angemeldeten Benutzers zu ermitteln.
 */
@Component
public class CurrentUser {

    /**
     * Liest die Benutzer-Kennung aus dem Anmeldekontext aus.
     *
     * @param authentication der Anmeldekontext der aktuellen Anfrage
     * @return die Kennung des angemeldeten Benutzers
     * @throws IllegalStateException wenn kein angemeldeter Benutzer vorhanden
     *                               ist
     */
    public UUID requireUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new IllegalStateException("Authenticated user is required");
        }
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return UUID.fromString(jwt.getSubject());
        }
        return UUID.fromString(authentication.getName());
    }
}
