package com.disciplica.shared.user;

/**
 * Aussehen des Avatars eines Benutzers. Alle Werte sind als Bezeichner
 * (z.&nbsp;B. Farb- oder Stilnamen) hinterlegt und werden beim Zeichnen des
 * Avatars ausgewertet.
 *
 * @param bodySize   Körpergröße bzw. Statur des Avatars
 * @param shirtColor Farbe des Oberteils
 * @param skinColor  Hautfarbe
 * @param hairColor  Haarfarbe
 * @param hairBangs  Art des Ponys bzw. der Stirnfransen
 * @param hairStyle  Frisur
 * @param extra      zusätzliches Ausstattungsmerkmal
 */
public record AvatarProfileDto(
        String bodySize,
        String shirtColor,
        String skinColor,
        String hairColor,
        String hairBangs,
        String hairStyle,
        String extra
) {
}
