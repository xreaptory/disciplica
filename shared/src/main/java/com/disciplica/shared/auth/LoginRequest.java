package com.disciplica.shared.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Anmeldedaten für die klassische Anmeldung mit E-Mail und Passwort.
 *
 * @param email    E-Mail-Adresse des Benutzers (muss ein gültiges Format haben)
 * @param password das eingegebene Passwort im Klartext
 */
public record LoginRequest(
        @Email(message = "Email must be valid")
        @NotBlank(message = "Email is required") String email,
        @NotBlank(message = "Password is required") String password
) {
}
