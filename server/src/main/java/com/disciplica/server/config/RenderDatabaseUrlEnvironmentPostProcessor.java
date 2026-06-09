package com.disciplica.server.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Wandelt die von der Hosting-Plattform Render bereitgestellte
 * Datenbank-Adresse in ein für Spring nutzbares Format um.
 * <p>
 * Render stellt die Verbindung als {@code DATABASE_URL} im Format
 * {@code postgres://benutzer:passwort@host:port/datenbank} bereit. Spring
 * erwartet jedoch eine {@code jdbc:postgresql://}-Adresse sowie getrennte
 * Felder für Benutzername und Passwort. Dieser Post-Processor erkennt eine
 * solche Adresse vor dem Start und ergänzt die passenden Spring-Eigenschaften.
 */
public class RenderDatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "renderDatabaseUrl";

    /**
     * Liest die Datenbank-Adresse aus der Umgebung, zerlegt sie und ergänzt
     * daraus die Spring-Datenquellen-Eigenschaften. Ist keine
     * Postgres-Adresse vorhanden, geschieht nichts.
     *
     * @param environment die Spring-Umgebung, deren Eigenschaften ergänzt
     *                    werden
     * @param application die startende Anwendung (hier nicht verwendet)
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = firstPresent(
                environment.getProperty("DATABASE_URL"),
                environment.getProperty("SPRING_DATASOURCE_URL")
        );

        if (databaseUrl == null || !isPostgresUrl(databaseUrl)) {
            return;
        }

        URI uri = URI.create(databaseUrl);
        Map<String, Object> properties = new HashMap<>();
        properties.put("spring.datasource.url", toJdbcUrl(uri));

        Credentials credentials = credentials(uri);
        if (credentials.username() != null && environment.getProperty("DATABASE_USERNAME") == null) {
            properties.put("spring.datasource.username", credentials.username());
        }
        if (credentials.password() != null && environment.getProperty("DATABASE_PASSWORD") == null) {
            properties.put("spring.datasource.password", credentials.password());
        }

        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
    }

    /**
     * Prüft, ob die angegebene Adresse auf eine PostgreSQL-Datenbank verweist.
     *
     * @param value die zu prüfende Adresse
     * @return {@code true}, wenn es sich um eine Postgres-Adresse handelt
     */
    private static boolean isPostgresUrl(String value) {
        return value.startsWith("postgres://") || value.startsWith("postgresql://");
    }

    /**
     * Baut aus einer Postgres-Adresse die entsprechende JDBC-Adresse zusammen.
     *
     * @param uri die zerlegte Datenbank-Adresse
     * @return die {@code jdbc:postgresql://}-Adresse für Spring
     */
    private static String toJdbcUrl(URI uri) {
        StringBuilder jdbcUrl = new StringBuilder("jdbc:postgresql://");
        jdbcUrl.append(uri.getHost());
        if (uri.getPort() > 0) {
            jdbcUrl.append(':').append(uri.getPort());
        }
        jdbcUrl.append(uri.getRawPath() == null || uri.getRawPath().isBlank() ? "/" : uri.getRawPath());
        if (uri.getRawQuery() != null && !uri.getRawQuery().isBlank()) {
            jdbcUrl.append('?').append(uri.getRawQuery());
        }
        return jdbcUrl.toString();
    }

    /**
     * Liest Benutzername und Passwort aus dem Benutzer-Teil der Adresse aus.
     *
     * @param uri die zerlegte Datenbank-Adresse
     * @return die gefundenen Zugangsdaten; beide Felder sind {@code null},
     *         wenn keine Angaben vorhanden sind
     */
    private static Credentials credentials(URI uri) {
        String userInfo = uri.getRawUserInfo();
        if (userInfo == null || userInfo.isBlank()) {
            return new Credentials(null, null);
        }

        String[] parts = userInfo.split(":", 2);
        String username = decode(parts[0]);
        String password = parts.length > 1 ? decode(parts[1]) : null;
        return new Credentials(username, password);
    }

    /**
     * Dekodiert einen URL-kodierten Wert (z.&nbsp;B. ein Passwort mit
     * Sonderzeichen).
     *
     * @param value der kodierte Wert
     * @return der dekodierte Klartext
     */
    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    /**
     * Gibt den ersten der beiden Werte zurück, der nicht leer ist.
     *
     * @param first  bevorzugter Wert
     * @param second Ausweichwert
     * @return den ersten nicht-leeren Wert oder {@code null}, wenn beide
     *         leer sind
     */
    private static String firstPresent(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }

    /**
     * Zugangsdaten (Benutzername und Passwort) aus der Datenbank-Adresse.
     *
     * @param username der Benutzername oder {@code null}
     * @param password das Passwort oder {@code null}
     */
    private record Credentials(String username, String password) {
    }
}
