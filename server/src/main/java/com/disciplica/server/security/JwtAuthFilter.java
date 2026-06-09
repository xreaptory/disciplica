package com.disciplica.server.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Schlanker Filter zur Prüfung von JWT-Tokens.
 *
 * <p>Er ersetzt den {@code BearerTokenAuthenticationFilter} von Spring
 * Security, damit in {@code authorizeHttpRequests} {@code permitAll()}
 * verwendet werden kann, ohne dass der Bearer-Token-Filter die Anfrage zuvor
 * abfängt und mit 401 ablehnt.
 *
 * <p>Verhalten:
 * <ul>
 *   <li>Enthält die Anfrage einen gültigen {@code Authorization: Bearer …}
 *       -Header, wird das JWT ausgelesen und ein angemeldeter
 *       {@link JwtAuthenticationToken} im {@link SecurityContextHolder}
 *       hinterlegt.</li>
 *   <li>Fehlt der Header oder ist das Token ungültig bzw. abgelaufen, tut der
 *       Filter nichts — die Berechtigungsprüfung lehnt die Anfrage später ab,
 *       falls der Endpunkt eine Anmeldung erfordert.</li>
 * </ul>
 */
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;

    /**
     * Erzeugt den Filter mit der Komponente zum Prüfen von JWT-Tokens.
     *
     * @param jwtDecoder Komponente zum Prüfen und Auslesen von JWT-Tokens
     */
    public JwtAuthFilter(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * Prüft den {@code Authorization}-Header und legt bei gültigem Token den
     * Anmeldekontext an. Anschließend wird die Anfrage in jedem Fall an die
     * nächste Stelle der Filterkette weitergegeben.
     *
     * @param request  die eingehende HTTP-Anfrage
     * @param response die HTTP-Antwort
     * @param chain    die restliche Filterkette
     * @throws ServletException bei einem Fehler in der Filterkette
     * @throws IOException      bei einem Ein-/Ausgabefehler
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Jwt jwt = jwtDecoder.decode(token);
                // Leere Rechteliste übergeben — die Anwendung wertet die JWT-Claims direkt aus.
                // Der zweiargumentige Konstruktor ruft intern setAuthenticated(true) auf.
                JwtAuthenticationToken auth = new JwtAuthenticationToken(jwt, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ignored) {
                // Ungültiges / abgelaufenes Token — ohne Anmeldung fortfahren.
                // Die Berechtigungsprüfung lehnt die Anfrage ab, falls nötig.
            }
        }
        chain.doFilter(request, response);
    }
}
