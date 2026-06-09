package com.disciplica.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Konfigurationswerte für die Anmeldung über Google (OAuth).
 * <p>
 * Die Werte werden aus der Konfiguration mit dem Präfix
 * {@code disciplica.google} eingelesen.
 *
 * @param clientId      die bei Google registrierte Client-ID
 * @param clientSecret  das geheime Schlüsselwort der Anwendung
 * @param publicBaseUrl öffentlich erreichbare Basis-Adresse des Servers,
 *                      auf die Google nach der Anmeldung zurückleitet
 */
@ConfigurationProperties(prefix = "disciplica.google")
public record GoogleProperties(
        String clientId,
        String clientSecret,
        String publicBaseUrl
) {
}
