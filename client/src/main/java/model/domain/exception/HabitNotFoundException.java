package model.domain.exception;

/**
 * Wird ausgelöst, wenn eine angeforderte Gewohnheit nicht gefunden werden
 * kann.
 */
public class HabitNotFoundException extends Exception {

    /**
     * Erzeugt die Ausnahme mit einer Fehlermeldung.
     *
     * @param message die Beschreibung des Fehlers
     */
    public HabitNotFoundException(String message) {
        super(message);
    }

    /**
     * Erzeugt die Ausnahme mit einer Fehlermeldung und einer Ursache.
     *
     * @param message die Beschreibung des Fehlers
     * @param cause   die zugrunde liegende Ursache
     */
    public HabitNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
