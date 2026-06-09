package com.disciplica.shared.user;

import java.util.UUID;

/**
 * Öffentliches Profil eines Benutzers mit Spielfortschritt und Avatar.
 *
 * @param id       eindeutige Kennung des Benutzers
 * @param username Benutzername
 * @param email    E-Mail-Adresse des Benutzers
 * @param level    aktuelles Level
 * @param xp       gesammelte Erfahrungspunkte
 * @param health   aktuelle Lebenspunkte
 * @param gold     verfügbares Gold (Spielwährung)
 * @param avatar   Aussehen des Avatars
 */
public record UserProfile(
        UUID id,
        String username,
        String email,
        int level,
        int xp,
        int health,
        int gold,
        AvatarProfileDto avatar
) {
}
