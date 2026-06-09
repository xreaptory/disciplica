package com.disciplica.shared.party;

import jakarta.validation.constraints.NotBlank;

/**
 * Anfrage zum Einladen eines Benutzers in eine Gruppe (Party).
 *
 * @param usernameOrEmail Benutzername oder E-Mail-Adresse der einzuladenden
 *                        Person
 */
public record InvitePartyRequest(
        @NotBlank String usernameOrEmail
) {
}
