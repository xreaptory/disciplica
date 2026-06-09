package com.disciplica.shared.party;

import java.time.Instant;
import java.util.UUID;

/**
 * Eine einzelne Chat-Nachricht innerhalb einer Gruppe (Party).
 *
 * @param id             eindeutige Kennung der Nachricht
 * @param partyId        Kennung der Gruppe, zu der die Nachricht gehört
 * @param senderId       Kennung des Benutzers, der die Nachricht gesendet hat
 * @param senderUsername Benutzername des Absenders (für die Anzeige)
 * @param message        der Textinhalt der Nachricht
 * @param createdAt      Zeitpunkt, zu dem die Nachricht gesendet wurde
 */
public record ChatMessageDto(
        UUID id,
        UUID partyId,
        UUID senderId,
        String senderUsername,
        String message,
        Instant createdAt
) {
}
