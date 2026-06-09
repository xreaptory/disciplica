package com.disciplica.shared.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Anfrage zum Aktualisieren des Avatar-Aussehens eines Benutzers. Alle
 * Merkmale müssen gesetzt sein (höchstens 32 Zeichen je Feld).
 *
 * @param bodySize   Körpergröße bzw. Statur des Avatars
 * @param shirtColor Farbe des Oberteils
 * @param skinColor  Hautfarbe
 * @param hairColor  Haarfarbe
 * @param hairBangs  Art des Ponys bzw. der Stirnfransen
 * @param hairStyle  Frisur
 * @param extra      zusätzliches Ausstattungsmerkmal
 */
public record UpdateAvatarProfileRequest(
        @NotBlank @Size(max = 32) String bodySize,
        @NotBlank @Size(max = 32) String shirtColor,
        @NotBlank @Size(max = 32) String skinColor,
        @NotBlank @Size(max = 32) String hairColor,
        @NotBlank @Size(max = 32) String hairBangs,
        @NotBlank @Size(max = 32) String hairStyle,
        @NotBlank @Size(max = 32) String extra
) {
}
