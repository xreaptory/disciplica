package com.disciplica.server.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Spring-MVC-Interceptor, der die Anmeldung auf geschützten Endpunkten
 * erzwingt.
 *
 * <p>Er läuft, NACHDEM das {@code DispatcherServlet} die Anfrage erhalten hat,
 * also vollständig außerhalb der Filterkette von Spring Security. Öffentliche
 * Pfade werden anhand ihres URI-Präfixes freigegeben; alle anderen Pfade
 * erfordern ein gültiges JWT (das vom {@link JwtAuthFilter} in den
 * SecurityContext gelegt wird).
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    /**
     * Prüft, ob ein Pfad ohne Anmeldung erreichbar sein soll.
     *
     * @param uri der angefragte Pfad
     * @return {@code true}, wenn der Pfad öffentlich ist
     */
    private static boolean isPublicUri(String uri) {
        return uri == null
                || uri.equals("/")
                || uri.equals("/status")
                || uri.equals("/healthz")
                || uri.equals("/actuator/health")
                // Fehler-Weiterleitung von Spring Boot — muss öffentlich sein, damit die
                // echte Fehlerantwort den Client erreicht und nicht von diesem Interceptor
                // verschluckt wird.
                || uri.equals("/error")
                || uri.startsWith("/error/")
                || uri.equals("/auth")
                || uri.startsWith("/auth/")
                || uri.equals("/ws")
                || uri.startsWith("/ws/");
    }

    /**
     * Wird vor jeder Controller-Methode aufgerufen und entscheidet, ob die
     * Anfrage weiterverarbeitet werden darf. Öffentliche Pfade werden
     * durchgelassen; für geschützte Pfade muss ein gültiges JWT vorliegen,
     * andernfalls wird die Antwort {@code 401 Unauthorized} gesendet.
     *
     * @param request  die eingehende HTTP-Anfrage
     * @param response die HTTP-Antwort
     * @param handler  die Ziel-Controller-Methode (hier nicht verwendet)
     * @return {@code true}, wenn die Anfrage weiterverarbeitet werden darf,
     *         sonst {@code false}
     * @throws Exception wenn das Schreiben der Fehlerantwort fehlschlägt
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String requestUri  = request.getRequestURI();
        String servletPath = request.getServletPath();
        String pathInfo    = request.getPathInfo();
        String contextPath = request.getContextPath();

        // Den für den Abgleich maßgeblichen Pfad bestimmen.
        // servletPath wird bevorzugt, da es das Kontextpfad-Präfix entfernt.
        String effectivePath = (servletPath != null && !servletPath.isEmpty()) ? servletPath : requestUri;

        log.info("[AuthInterceptor] method={} requestURI={} servletPath={} pathInfo={} contextPath={} effectivePath={}",
                request.getMethod(), requestUri, servletPath, pathInfo, contextPath, effectivePath);

        if (isPublicUri(requestUri) || isPublicUri(effectivePath)) {
            log.info("[AuthInterceptor] PUBLIC path — allowing through");
            return true;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean authenticated = auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);

        log.info("[AuthInterceptor] PROTECTED path — authenticated={} principal={}",
                authenticated, auth == null ? "null" : auth.getClass().getSimpleName());

        if (!authenticated) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"Unauthorized\",\"path\":\"" + requestUri + "\"}");
            return false;
        }
        return true;
    }
}
