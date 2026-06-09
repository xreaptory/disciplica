package com.disciplica.shared.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Daten für die Registrierung eines neuen Benutzerkontos.
 *
 * @param username gewünschter Benutzername (3 bis 32 Zeichen)
 * @param email    E-Mail-Adresse des Benutzers (muss gültig sein)
 * @param password gewähltes Passwort (10 bis 128 Zeichen)
 */
public record RegisterRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 32, message = "Username must be 3 to 32 characters") String username,
        @Email(message = "Email must be valid")
        @NotBlank(message = "Email is required") String email,
        @NotBlank(message = "Password is required")
        @Size(min = 10, max = 128, message = "Password must be 10 to 128 characters") String password
) {
}
