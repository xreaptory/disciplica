package com.disciplica.shared.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Anfrage zum Abschluss der Google-Anmeldung für die Desktop-Anwendung.
 * <p>
 * Der bei der Google-Anmeldung erhaltene Autorisierungscode wird an den
 * Server geschickt, der ihn gegen die Benutzer-Tokens eintauscht.
 *
 * @param code der von Google zurückgegebene Autorisierungscode
 */
public record GoogleDesktopCompleteRequest(
        @NotBlank String code
) {
}
