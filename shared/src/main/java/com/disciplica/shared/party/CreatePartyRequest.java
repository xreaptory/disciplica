package com.disciplica.shared.party;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Anfrage zum Erstellen einer neuen Gruppe (Party).
 *
 * @param name der Name der neuen Gruppe (höchstens 80 Zeichen)
 */
public record CreatePartyRequest(
        @NotBlank @Size(max = 80) String name
) {
}
