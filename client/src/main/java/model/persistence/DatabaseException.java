package model.persistence;

/**
 * Laufzeit-Ausnahme für Fehler beim Datenbankzugriff.
 */
public class DatabaseException extends RuntimeException {

    /**
     * Erzeugt die Ausnahme mit Meldung und Ursache.
     *
     * @param message die Beschreibung des Fehlers
     * @param cause   die zugrunde liegende Ursache
     */
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Erzeugt die Ausnahme mit einer Meldung.
     *
     * @param message die Beschreibung des Fehlers
     */
    public DatabaseException(String message) {
        super(message);
    }
}
