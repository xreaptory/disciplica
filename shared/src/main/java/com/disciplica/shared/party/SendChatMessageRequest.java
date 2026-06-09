package com.disciplica.shared.party;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Anfrage zum Senden einer Chat-Nachricht in einer Gruppe (Party).
 *
 * @param message der Textinhalt der Nachricht (höchstens 1000 Zeichen)
 */
public record SendChatMessageRequest(
        @NotBlank @Size(max = 1000) String message
) {
}
