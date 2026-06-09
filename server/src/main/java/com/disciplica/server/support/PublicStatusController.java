package com.disciplica.server.support;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Öffentlicher Controller, über den der Betriebszustand des Servers ohne
 * Anmeldung abgefragt werden kann (z.&nbsp;B. für Health-Checks der
 * Hosting-Plattform).
 */
@RestController
public class PublicStatusController {
<<<<<<< HEAD
    private static final String BUILD_MARKER = "fix-parameters-v1";
=======
    private static final String BUILD_MARKER = "fix-500-v1";
>>>>>>> 25093ff917d7ffc71f81bfbbb7d8f1fc752561ca

    /**
     * Antwort auf die Startseite {@code /}; liefert denselben Status wie
     * {@link #status()}.
     *
     * @return eine Statusmeldung als Schlüssel-Wert-Paare
     */
    @GetMapping("/")
    public Map<String, String> index() {
        return status();
    }

    /**
     * Gibt den aktuellen Betriebszustand des Servers zurück.
     *
     * @return eine Statusmeldung mit Dienstname, Zustand und Build-Kennung
     */
    @GetMapping({"/status", "/healthz"})
    public Map<String, String> status() {
        return Map.of(
                "service", "disciplica-api-now5",
                "status", "ok",
                "build", BUILD_MARKER
        );
    }
}
