package com.disciplica.shared.party;

import java.time.Instant;
import java.util.UUID;

/**
 * Beschreibt ein einzelnes Mitglied einer Gruppe (Party).
 *
 * @param userId   Kennung des Benutzers
 * @param username Benutzername des Mitglieds (für die Anzeige)
 * @param role     Rolle des Mitglieds in der Gruppe (z.&nbsp;B. Leiter
 *                 oder Mitglied)
 * @param joinedAt Zeitpunkt, zu dem das Mitglied der Gruppe beigetreten ist
 */
public record PartyMemberDto(
        UUID userId,
        String username,
        String role,
        Instant joinedAt
) {
}
