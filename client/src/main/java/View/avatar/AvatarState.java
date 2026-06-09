package View.avatar;

import com.disciplica.shared.user.AvatarProfileDto;
import com.disciplica.shared.user.UpdateAvatarProfileRequest;

/**
 * Veränderbarer Zustand des Avatar-Aussehens innerhalb der Oberfläche.
 * <p>
 * Hält die einzelnen Merkmale als anzeigefreundliche Texte (z.&nbsp;B.
 * „Medium“) und wandelt sie bei Bedarf in die technische Form für Server und
 * Speicherung um.
 */
public class AvatarState {
    private String bodySize = "Medium";
    private String shirtColor = "Blue";
    private String skinColor = "Warm";
    private String hairColor = "Brown";
    private String hairBangs = "None";
    private String hairStyle = "Short";
    private String extra = "None";

    /**
     * {@return ein Avatar-Zustand mit Standardwerten}
     */
    public static AvatarState defaults() {
        return new AvatarState();
    }

    /**
     * Erzeugt einen Avatar-Zustand aus einem vom Server gelieferten DTO.
     *
     * @param dto das Avatar-DTO (darf {@code null} sein)
     * @return der entsprechende Avatar-Zustand (bei {@code null} die
     *         Standardwerte)
     */
    public static AvatarState fromDto(AvatarProfileDto dto) {
        AvatarState state = defaults();
        if (dto == null) {
            return state;
        }
        state.setBodySize(display(dto.bodySize(), state.bodySize));
        state.setShirtColor(display(dto.shirtColor(), state.shirtColor));
        state.setSkinColor(display(dto.skinColor(), state.skinColor));
        state.setHairColor(display(dto.hairColor(), state.hairColor));
        state.setHairBangs(display(dto.hairBangs(), state.hairBangs));
        state.setHairStyle(display(dto.hairStyle(), state.hairStyle));
        state.setExtra(display(dto.extra(), state.extra));
        return state;
    }

    /**
     * {@return ein Avatar-DTO mit den Werten in technischer Form}
     */
    public AvatarProfileDto toDto() {
        return new AvatarProfileDto(
                value(bodySize),
                value(shirtColor),
                value(skinColor),
                value(hairColor),
                value(hairBangs),
                value(hairStyle),
                value(extra)
        );
    }

    /**
     * {@return eine Aktualisierungsanfrage mit den Werten in technischer Form}
     */
    public UpdateAvatarProfileRequest toUpdateRequest() {
        return new UpdateAvatarProfileRequest(
                value(bodySize),
                value(shirtColor),
                value(skinColor),
                value(hairColor),
                value(hairBangs),
                value(hairStyle),
                value(extra)
        );
    }

    /** {@return die Körpergröße (Anzeigeform)} */
    public String getBodySize() {
        return bodySize;
    }

    /**
     * Setzt die Körpergröße (leere Werte werden durch den Standard ersetzt).
     *
     * @param bodySize die neue Körpergröße
     */
    public void setBodySize(String bodySize) {
        this.bodySize = safe(bodySize, "Medium");
    }

    /** {@return die Farbe des Oberteils (Anzeigeform)} */
    public String getShirtColor() {
        return shirtColor;
    }

    /**
     * Setzt die Farbe des Oberteils.
     *
     * @param shirtColor die neue Farbe
     */
    public void setShirtColor(String shirtColor) {
        this.shirtColor = safe(shirtColor, "Blue");
    }

    /** {@return die Hautfarbe (Anzeigeform)} */
    public String getSkinColor() {
        return skinColor;
    }

    /**
     * Setzt die Hautfarbe.
     *
     * @param skinColor die neue Hautfarbe
     */
    public void setSkinColor(String skinColor) {
        this.skinColor = safe(skinColor, "Warm");
    }

    /** {@return die Haarfarbe (Anzeigeform)} */
    public String getHairColor() {
        return hairColor;
    }

    /**
     * Setzt die Haarfarbe.
     *
     * @param hairColor die neue Haarfarbe
     */
    public void setHairColor(String hairColor) {
        this.hairColor = safe(hairColor, "Brown");
    }

    /** {@return die Art des Ponys (Anzeigeform)} */
    public String getHairBangs() {
        return hairBangs;
    }

    /**
     * Setzt die Art des Ponys.
     *
     * @param hairBangs die neue Pony-Art
     */
    public void setHairBangs(String hairBangs) {
        this.hairBangs = safe(hairBangs, "None");
    }

    /** {@return die Frisur (Anzeigeform)} */
    public String getHairStyle() {
        return hairStyle;
    }

    /**
     * Setzt die Frisur.
     *
     * @param hairStyle die neue Frisur
     */
    public void setHairStyle(String hairStyle) {
        this.hairStyle = safe(hairStyle, "Short");
    }

    /** {@return das zusätzliche Ausstattungsmerkmal (Anzeigeform)} */
    public String getExtra() {
        return extra;
    }

    /**
     * Setzt das zusätzliche Ausstattungsmerkmal.
     *
     * @param extra das neue Merkmal
     */
    public void setExtra(String extra) {
        this.extra = safe(extra, "None");
    }

    /**
     * Gibt den Wert zurück oder den Ersatzwert, falls er leer ist.
     *
     * @param value    der zu prüfende Wert
     * @param fallback der Ersatzwert
     * @return der Wert oder der Ersatzwert
     */
    private static String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    /**
     * Wandelt eine Anzeigeform in die technische Form um (klein geschrieben,
     * Leerzeichen durch Bindestriche ersetzt).
     *
     * @param display die Anzeigeform
     * @return die technische Form
     */
    private static String value(String display) {
        return display == null ? "" : display.trim().toLowerCase().replace(' ', '-');
    }

    /**
     * Wandelt die technische Form in eine lesbare Anzeigeform um (jedes Wort
     * groß beginnend).
     *
     * @param value    die technische Form
     * @param fallback der Ersatzwert, falls leer
     * @return die Anzeigeform
     */
    private static String display(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String normalized = value.replace('-', ' ').replace('_', ' ').trim();
        StringBuilder builder = new StringBuilder();
        for (String part : normalized.split("\\s+")) {
            if (!part.isBlank()) {
                builder.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    builder.append(part.substring(1).toLowerCase());
                }
                builder.append(' ');
            }
        }
        return builder.toString().trim();
    }
}
