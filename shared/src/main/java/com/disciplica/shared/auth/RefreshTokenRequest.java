package com.disciplica.shared.auth;

import jakarta.validation.constraints.NotBlank;

/**
 * Anfrage zum Erneuern eines abgelaufenen Access-Tokens.
 *
 * @param refreshToken das gültige Refresh-Token, mit dem ein neues
 *                     Access-Token ausgestellt werden soll
 */
public record RefreshTokenRequest(
        @NotBlank String refreshToken
) {
}
