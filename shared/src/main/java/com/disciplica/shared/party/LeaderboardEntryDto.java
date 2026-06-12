package com.disciplica.shared.party;

import java.util.UUID;

/**
 * Ein Eintrag der Gruppen-Bestenliste (Leaderboard).
 *
 * @param rank     Platzierung innerhalb der Gruppe (1 = bester Platz)
 * @param userId   Kennung des Benutzers
 * @param username Benutzername (für die Anzeige)
 * @param role     Rolle in der Gruppe (z.&nbsp;B. Leiter oder Mitglied)
 * @param level    aktuelles Level des Benutzers
 * @param xp       gesammelte Erfahrungspunkte
 * @param gold     verfügbares Gold
 */
public record LeaderboardEntryDto(
        int rank,
        UUID userId,
        String username,
        String role,
        int level,
        int xp,
        int gold
) {
}
