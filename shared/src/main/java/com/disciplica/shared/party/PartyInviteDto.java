package com.disciplica.shared.party;

import java.time.Instant;
import java.util.UUID;

/**
 * Beschreibt eine Einladung in eine Gruppe (Party).
 *
 * @param id            eindeutige Kennung der Einladung
 * @param partyId       Kennung der Gruppe, in die eingeladen wird
 * @param partyName     Name der Gruppe (für die Anzeige)
 * @param invitedUserId Kennung des eingeladenen Benutzers
 * @param status        aktueller Status der Einladung (z.&nbsp;B. offen,
 *                      angenommen oder abgelehnt)
 * @param createdAt     Zeitpunkt, zu dem die Einladung erstellt wurde
 */
public record PartyInviteDto(
        UUID id,
        UUID partyId,
        String partyName,
        UUID invitedUserId,
        String status,
        Instant createdAt
) {
}
