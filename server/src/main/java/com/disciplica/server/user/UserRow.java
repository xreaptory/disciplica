package com.disciplica.server.user;

import java.util.UUID;

/**
 * Eine Zeile der Benutzertabelle, wie sie aus der Datenbank gelesen wird.
 *
 * @param id       eindeutige Kennung des Benutzers
 * @param username Benutzername
 * @param email    E-Mail-Adresse
 * @param level    aktuelles Level
 * @param xp       gesammelte Erfahrungspunkte
 * @param health   aktuelle Lebenspunkte
 * @param gold     verfügbares Gold (Spielwährung)
 */
public record UserRow(
        UUID id,
        String username,
        String email,
        int level,
        int xp,
        int health,
        int gold
) {
}
