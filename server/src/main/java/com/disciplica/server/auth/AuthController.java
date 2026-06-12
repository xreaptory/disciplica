package com.disciplica.server.auth;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.disciplica.server.config.GoogleProperties;
import com.disciplica.server.security.CurrentUser;
import com.disciplica.server.support.ApiException;
import com.disciplica.shared.auth.AuthResponse;
import com.disciplica.shared.auth.GoogleDesktopCompleteRequest;
import com.disciplica.shared.auth.GoogleLoginRequest;
import com.disciplica.shared.auth.LoginRequest;
import com.disciplica.shared.auth.RefreshTokenRequest;
import com.disciplica.shared.auth.RegisterRequest;
import com.disciplica.shared.user.SpendGoldRequest;
import com.disciplica.shared.user.UpdateAvatarProfileRequest;
import com.disciplica.shared.user.UserProfile;

/**
 * REST-Controller für Anmeldung, Registrierung und Profilverwaltung.
 * <p>
 * Neben der klassischen Anmeldung mit E-Mail und Passwort wird auch die
 * Anmeldung über Google unterstützt. Für die Desktop-Anwendung wird dabei ein
 * mehrstufiger Ablauf über kurzlebige, im Speicher gehaltene Zwischencodes
 * verwendet.
 */
@RestController
@RequestMapping
public class AuthController {
    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_SCOPE = "openid email profile";

    private final AuthService authService;
    private final CurrentUser currentUser;
    private final GoogleProperties googleProperties;
    private final Map<String, DesktopOAuthRequest> desktopRequests = new ConcurrentHashMap<>();
    private final Map<String, DesktopOAuthToken> desktopTokens = new ConcurrentHashMap<>();

    /**
     * Erzeugt den Controller mit seinen Abhängigkeiten.
     *
     * @param authService      der Dienst mit der eigentlichen Anmeldelogik
     * @param currentUser      Hilfsmittel zum Ermitteln des angemeldeten
     *                         Benutzers
     * @param googleProperties die Google-OAuth-Konfiguration
     */
    public AuthController(AuthService authService, CurrentUser currentUser, GoogleProperties googleProperties) {
        this.authService = authService;
        this.currentUser = currentUser;
        this.googleProperties = googleProperties;
    }

    /**
     * Registriert ein neues Benutzerkonto.
     *
     * @param request die Registrierungsdaten
     * @return die Tokens und das Profil des neu angelegten Benutzers
     */
    @PostMapping("/auth/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /**
     * Meldet einen Benutzer mit E-Mail und Passwort an.
     *
     * @param request die Anmeldedaten
     * @return die Tokens und das Profil des Benutzers
     */
    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    /**
     * Meldet einen Benutzer über ein Google-ID-Token an.
     *
     * @param request das Google-ID-Token
     * @return die Tokens und das Profil des Benutzers
     */
    @PostMapping("/auth/google")
    public AuthResponse google(@Valid @RequestBody GoogleLoginRequest request) {
        return authService.google(request);
    }

    /**
     * Startet die Google-Anmeldung für die Desktop-Anwendung und leitet zur
     * Google-Anmeldeseite weiter.
     *
     * @param appRedirectUri die lokale Adresse, an die die Desktop-Anwendung
     *                       nach der Anmeldung zurückgeleitet werden möchte
     * @return eine Weiterleitung zu Google oder eine Fehlerantwort, falls
     *         Google-OAuth nicht konfiguriert ist
     */
    @GetMapping("/auth/google/desktop/start")
    public ResponseEntity<?> googleDesktopStart(@RequestParam String appRedirectUri) {
        if (!isDesktopGoogleOAuthConfigured()) {
            // Direkt JSON zurückgeben — jeden Ausnahme-/Handler-Pfad vermeiden, der einen 500 erzeugen könnte.
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .header("Content-Type", "application/json")
                    .body(java.util.Map.of("error",
                            "Google OAuth is not configured. Set GOOGLE_CLIENT_SECRET in Render → Environment."));
        }
        cleanupDesktopOAuth();
        String state = UUID.randomUUID().toString();
        String serverRedirectUri = serverGoogleRedirectUri();
        desktopRequests.put(state, new DesktopOAuthRequest(appRedirectUri, Instant.now().plusSeconds(300)));
        String googleUri = GOOGLE_AUTH_URL
                + "?client_id=" + urlEncode(googleProperties.clientId())
                + "&redirect_uri=" + urlEncode(serverRedirectUri)
                + "&response_type=code"
                + "&scope=" + urlEncode(GOOGLE_SCOPE)
                + "&state=" + urlEncode(state)
                + "&access_type=offline"
                + "&prompt=select_account";
        return redirect(googleUri);
    }

    /**
     * Nimmt die Rückleitung von Google entgegen, tauscht den Code gegen die
     * Benutzer-Tokens und leitet mit einem kurzlebigen Zwischencode an die
     * Desktop-Anwendung zurück.
     *
     * @param code  der von Google zurückgegebene Autorisierungscode
     * @param state der zuvor erzeugte Zustandswert zur Zuordnung der Anfrage
     * @return eine Weiterleitung an die Desktop-Anwendung mit Code oder Fehler
     */
    @GetMapping("/auth/google/desktop/callback")
    public ResponseEntity<?> googleDesktopCallback(@RequestParam String code, @RequestParam String state) {
        cleanupDesktopOAuth();
        DesktopOAuthRequest request = desktopRequests.remove(state);
        if (request == null || request.expiresAt().isBefore(Instant.now())) {
            return redirect("http://127.0.0.1/?error=" + urlEncode("Google login session expired"));
        }
        try {
            AuthResponse authResponse = authService.googleAuthorizationCode(code, serverGoogleRedirectUri());
            String desktopCode = UUID.randomUUID().toString();
            desktopTokens.put(desktopCode, new DesktopOAuthToken(authResponse, Instant.now().plusSeconds(120)));
            return redirect(request.appRedirectUri() + "?code=" + urlEncode(desktopCode));
        } catch (ApiException exception) {
            return redirect(request.appRedirectUri() + "?error=" + urlEncode(exception.getMessage()));
        } catch (RuntimeException exception) {
            return redirect(request.appRedirectUri() + "?error=" + urlEncode("Google sign-in failed on the server"));
        }
    }

    /**
     * Tauscht den von der Desktop-Anwendung erhaltenen Zwischencode gegen die
     * endgültigen Benutzer-Tokens.
     *
     * @param request der Zwischencode
     * @return die Tokens und das Profil des Benutzers
     * @throws ApiException wenn der Code unbekannt oder abgelaufen ist
     */
    @PostMapping("/auth/google/desktop/complete")
    public AuthResponse googleDesktopComplete(@Valid @RequestBody GoogleDesktopCompleteRequest request) {
        cleanupDesktopOAuth();
        DesktopOAuthToken token = desktopTokens.remove(request.code());
        if (token == null || token.expiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Google desktop login code expired");
        }
        return token.authResponse();
    }

    /**
     * Stellt mit einem gültigen Refresh-Token ein neues Access-Token aus.
     *
     * @param request das Refresh-Token
     * @return die erneuerten Tokens und das Profil des Benutzers
     */
    @PostMapping("/auth/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request);
    }

    /**
     * Meldet den Benutzer ab und macht das angegebene Refresh-Token ungültig.
     *
     * @param request das abzumeldende Refresh-Token
     */
    @PostMapping("/auth/logout")
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
    }

    /**
     * Gibt das Profil des aktuell angemeldeten Benutzers zurück.
     *
     * @param authentication der Anmeldekontext der Anfrage
     * @return das Profil des angemeldeten Benutzers
     */
    @GetMapping("/me")
    public UserProfile me(Authentication authentication) {
        UUID userId = currentUser.requireUserId(authentication);
        return authService.me(userId);
    }

    /**
     * Aktualisiert das Avatar-Aussehen des angemeldeten Benutzers.
     *
     * @param authentication der Anmeldekontext der Anfrage
     * @param request        die neuen Avatar-Merkmale
     * @return das aktualisierte Profil des Benutzers
     */
    @PatchMapping("/me/avatar")
    public UserProfile updateAvatar(Authentication authentication, @Valid @RequestBody UpdateAvatarProfileRequest request) {
        UUID userId = currentUser.requireUserId(authentication);
        return authService.updateAvatar(userId, request);
    }

    /**
     * Zieht dem angemeldeten Benutzer Gold ab (z.&nbsp;B. ein Kauf im
     * Avatar-Shop) und gibt das aktualisierte Profil zurück.
     *
     * @param authentication der Anmeldekontext der Anfrage
     * @param request        der abzuziehende Goldbetrag
     * @return das aktualisierte Profil des Benutzers
     */
    @PostMapping("/me/spend-gold")
    public UserProfile spendGold(Authentication authentication, @Valid @RequestBody SpendGoldRequest request) {
        UUID userId = currentUser.requireUserId(authentication);
        return authService.spendGold(userId, request.amount());
    }

    /**
     * Erzeugt eine HTTP-Weiterleitung (Status 302) auf die angegebene Adresse.
     *
     * @param location die Zieladresse
     * @return die Antwort mit gesetztem {@code Location}-Header
     */
    private ResponseEntity<?> redirect(String location) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(location));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    /**
     * Baut die Rückleitungsadresse zusammen, die bei Google hinterlegt ist.
     *
     * @return die vollständige Server-Rückleitungsadresse
     */
    private String serverGoogleRedirectUri() {
        String baseUrl = googleProperties.publicBaseUrl() == null || googleProperties.publicBaseUrl().isBlank()
                ? "http://localhost:8080"
                : googleProperties.publicBaseUrl();
        return baseUrl.replaceAll("/+$", "") + "/auth/google/desktop/callback";
    }

    /**
     * Kodiert einen Wert für die Verwendung in einer URL.
     *
     * @param value der zu kodierende Wert
     * @return der URL-kodierte Wert
     */
    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Prüft, ob die Google-Desktop-Anmeldung vollständig konfiguriert ist.
     *
     * @return {@code true}, wenn Client-ID und Client-Secret gesetzt sind
     */
    private boolean isDesktopGoogleOAuthConfigured() {
        return googleProperties.clientId() != null && !googleProperties.clientId().isBlank()
                && googleProperties.clientSecret() != null && !googleProperties.clientSecret().isBlank();
    }

    /**
     * Entfernt abgelaufene Desktop-OAuth-Anfragen und -Tokens aus dem Speicher.
     */
    private void cleanupDesktopOAuth() {
        Instant now = Instant.now();
        desktopRequests.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
        desktopTokens.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    /**
     * Zwischengespeicherte Desktop-OAuth-Anfrage.
     *
     * @param appRedirectUri die Rückleitungsadresse der Desktop-Anwendung
     * @param expiresAt      Zeitpunkt, ab dem die Anfrage verfällt
     */
    private record DesktopOAuthRequest(String appRedirectUri, Instant expiresAt) {
    }

    /**
     * Zwischengespeichertes Ergebnis einer erfolgreichen Google-Anmeldung.
     *
     * @param authResponse die bereitstehenden Benutzer-Tokens und das Profil
     * @param expiresAt    Zeitpunkt, ab dem der Zwischencode verfällt
     */
    private record DesktopOAuthToken(AuthResponse authResponse, Instant expiresAt) {
    }
}
