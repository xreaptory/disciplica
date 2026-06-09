package model.springdata.service;

/**
 * Laufzeit-Ausnahme für Fehler in der Gewohnheits-Geschäftslogik
 * ({@link HabitService}).
 */
public class HabitServiceException extends RuntimeException {

    /**
     * Erzeugt die Ausnahme mit einer Fehlermeldung.
     *
     * @param message die Beschreibung des Fehlers
     */
    public HabitServiceException(String message) {
        super(message);
    }

    /**
     * Erzeugt die Ausnahme mit einer Fehlermeldung und einer Ursache.
     *
     * @param message die Beschreibung des Fehlers
     * @param cause   die zugrunde liegende Ursache
     */
    public HabitServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
