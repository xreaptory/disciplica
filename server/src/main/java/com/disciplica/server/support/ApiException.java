package com.disciplica.server.support;

import org.springframework.http.HttpStatus;

/**
 * Anwendungsfehler, der direkt einem HTTP-Statuscode zugeordnet ist.
 * <p>
 * Wird in den Service-Klassen geworfen und vom {@link ApiExceptionHandler}
 * in eine passende JSON-Fehlerantwort umgewandelt.
 */
public class ApiException extends RuntimeException {
    private final HttpStatus status;

    /**
     * Erzeugt einen neuen Anwendungsfehler.
     *
     * @param status  der HTTP-Status, der an den Client gesendet werden soll
     * @param message die für den Client bestimmte Fehlermeldung
     */
    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    /**
     * Gibt den zugeordneten HTTP-Status zurück.
     *
     * @return der HTTP-Status dieses Fehlers
     */
    public HttpStatus status() {
        return status;
    }
}
