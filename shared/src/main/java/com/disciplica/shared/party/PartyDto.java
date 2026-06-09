package com.disciplica.shared.party;

import java.util.List;
import java.util.UUID;

/**
 * Beschreibt eine Gruppe (Party) samt ihrer Mitglieder.
 *
 * @param id       eindeutige Kennung der Gruppe
 * @param name     Anzeigename der Gruppe
 * @param leaderId Kennung des Benutzers, der die Gruppe leitet
 * @param members  Liste aller Mitglieder der Gruppe
 */
public record PartyDto(
        UUID id,
        String name,
        UUID leaderId,
        List<PartyMemberDto> members
) {
}
