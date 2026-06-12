package com.disciplica.shared.auth;

import com.disciplica.shared.user.UserProfile;

/**
 * Antwort des Servers auf eine erfolgreiche Anmeldung oder Registrierung.
 * <p>
 * Enthält die beiden Tokens für die weitere Kommunikation sowie das Profil
 * des angemeldeten Benutzers.
 *
 * @param accessToken  kurzlebiges Token, das jeder geschützten Anfrage
 *                     mitgegeben wird
 * @param refreshToken langlebiges Token, mit dem ein neues Access-Token
 *                     angefordert werden kann
 * @param user         Profildaten des angemeldeten Benutzers
 * @param newUser      {@code true}, wenn mit dieser Anmeldung gerade ein neues
 *                     Konto angelegt wurde (steuert das Onboarding)
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserProfile user,
        boolean newUser
) {
}
