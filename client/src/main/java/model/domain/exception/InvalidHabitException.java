package model.domain.exception;

/**
 * Wird ausgelöst, wenn eine Gewohnheit ungültige Daten enthält (z.&nbsp;B.
 * ein leerer Name oder ein unzulässiger Wert).
 */
public class InvalidHabitException extends Exception {

    /**
     * Erzeugt die Ausnahme mit einer Fehlermeldung.
     *
     * @param message die Beschreibung des Fehlers
     */
    public InvalidHabitException(String message) {
        super(message);
    }

    /**
     * Erzeugt die Ausnahme mit einer Fehlermeldung und einer Ursache.
     *
     * @param message die Beschreibung des Fehlers
     * @param cause   die zugrunde liegende Ursache
     */
    public InvalidHabitException(String message, Throwable cause) {
        super(message, cause);
    }
}
