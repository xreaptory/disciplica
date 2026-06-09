package com.disciplica.server.support;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Zentrale Fehlerbehandlung für alle REST-Controller.
 * <p>
 * Wandelt geworfene Ausnahmen in einheitliche JSON-Fehlerantworten um, statt
 * die Standard-HTML-Fehlerseite von Spring Boot auszuliefern.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /**
     * Behandelt bewusst geworfene {@link ApiException}en und gibt sie mit dem
     * hinterlegten HTTP-Status zurück.
     *
     * @param exception der aufgetretene Anwendungsfehler
     * @return eine JSON-Antwort mit Fehlermeldung und passendem Status
     */
    @ExceptionHandler(ApiException.class)
    ResponseEntity<Map<String, String>> handleApiException(ApiException exception) {
        return ResponseEntity.status(exception.status())
                .body(Map.of("error", exception.getMessage() != null ? exception.getMessage() : "error"));
    }

    /**
     * Behandelt Validierungsfehler (z.&nbsp;B. ungültige Eingabefelder) und
     * fasst die einzelnen Feldfehler zu einer Meldung zusammen.
     *
     * @param exception die Validierungs-Ausnahme
     * @return eine JSON-Antwort mit Status {@code 400 Bad Request}
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(error -> error.getDefaultMessage() == null
                        ? error.getField() + " is invalid"
                        : error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("\n"));
        return ResponseEntity.badRequest().body(Map.of(
                "error", message.isBlank() ? "Request validation failed" : message
        ));
    }

    /**
     * Auffangbehandlung für alle nicht eigens behandelten Ausnahmen. Gibt
     * eine JSON-Fehlerantwort statt der Standard-HTML-Seite zurück; der
     * vollständige Stacktrace wird protokolliert.
     *
     * @param exception die unerwartete Ausnahme
     * @return eine JSON-Antwort mit Status {@code 500 Internal Server Error}
     */
    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> handleUnexpected(Exception exception) {
        log.error("Unhandled exception — returning 500", exception);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "Internal server error");
        body.put("type", exception.getClass().getName());
        body.put("message", exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
