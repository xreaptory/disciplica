package com.disciplica.shared.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Anfrage für die Anmeldung über ein Google-Konto.
 *
 * @param idToken das von Google ausgestellte ID-Token, das die Identität
 *                des Benutzers bestätigt
 */
public record GoogleLoginRequest(
        @NotBlank String idToken
) {
}
