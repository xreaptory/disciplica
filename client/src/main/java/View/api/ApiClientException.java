package View.api;

/**
 * Laufzeit-Ausnahme für Fehler bei der Kommunikation mit dem Server
 * ({@link ApiClient}).
 */
public class ApiClientException extends RuntimeException {

    /**
     * Erzeugt die Ausnahme mit einer Fehlermeldung.
     *
     * @param message die Beschreibung des Fehlers
     */
    public ApiClientException(String message) {
        super(message);
    }

    /**
     * Erzeugt die Ausnahme mit einer Fehlermeldung und einer Ursache.
     *
     * @param message die Beschreibung des Fehlers
     * @param cause   die zugrunde liegende Ursache
     */
    public ApiClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
