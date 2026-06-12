package com.disciplica.server.auth;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.UUID;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.disciplica.server.config.GoogleProperties;
import com.disciplica.server.config.JwtProperties;
import com.disciplica.server.security.HashingService;
import com.disciplica.server.security.JwtService;
import com.disciplica.server.support.ApiException;
import com.disciplica.server.user.UserMapper;
import com.disciplica.server.user.UserRepository;
import com.disciplica.server.user.UserRow;
import com.disciplica.shared.auth.AuthResponse;
import com.disciplica.shared.auth.GoogleLoginRequest;
import com.disciplica.shared.auth.LoginRequest;
import com.disciplica.shared.auth.RefreshTokenRequest;
import com.disciplica.shared.auth.RegisterRequest;
import com.disciplica.shared.user.UpdateAvatarProfileRequest;
import com.disciplica.shared.user.UserProfile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

/**
 * Dienst mit der gesamten Anmeldelogik: Registrierung, Anmeldung mit
 * Passwort oder Google, Token-Erneuerung, Abmeldung und Profilpflege.
 * <p>
 * Greift direkt über {@link JdbcTemplate} auf die Datenbank zu und stellt
 * Access- sowie Refresh-Tokens aus.
 */
@Service
public class AuthService {
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final GoogleProperties googleProperties;
    private final HashingService hashingService;
    private final ObjectMapper objectMapper;

    /**
     * Erzeugt den Dienst mit allen benötigten Abhängigkeiten.
     *
     * @param jdbcTemplate     direkter Datenbankzugriff
     * @param passwordEncoder  Encoder zum Hashen und Prüfen von Passwörtern
     * @param userRepository   Zugriff auf die Benutzertabelle
     * @param userMapper       wandelt Datenbankzeilen in Profile um
     * @param jwtService       erstellt Access-Tokens
     * @param jwtProperties    JWT-Konfiguration (Gültigkeitsdauern)
     * @param googleProperties Google-OAuth-Konfiguration
     * @param hashingService   erzeugt Tokens und Hashwerte
     * @param objectMapper     liest JSON-Antworten von Google
     */
    public AuthService(JdbcTemplate jdbcTemplate,
                       PasswordEncoder passwordEncoder,
                       UserRepository userRepository,
                       UserMapper userMapper,
                       JwtService jwtService,
                       JwtProperties jwtProperties,
                       GoogleProperties googleProperties,
                       HashingService hashingService,
                       ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.googleProperties = googleProperties;
        this.hashingService = hashingService;
        this.objectMapper = objectMapper;
    }

    /**
     * Legt einen neuen Benutzer samt Zugangsdaten und leerem Avatar an.
     *
     * @param request die Registrierungsdaten
     * @return die ausgestellten Tokens und das Profil
     * @throws ApiException wenn Benutzername oder E-Mail bereits vergeben sind
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        try {
            UserRow user = userRepository.create(request.username(), request.email());
            jdbcTemplate.update("INSERT INTO user_credentials (user_id, password_hash) VALUES (?, ?)",
                    user.id(), passwordEncoder.encode(request.password()));
            jdbcTemplate.update("INSERT INTO avatar_profiles (user_id) VALUES (?)", user.id());
            return issueTokens(user, true);
        } catch (DuplicateKeyException exception) {
            throw new ApiException(HttpStatus.CONFLICT, "Username or email already exists");
        }
    }

    /**
     * Meldet einen Benutzer mit E-Mail und Passwort an.
     *
     * @param request die Anmeldedaten
     * @return die ausgestellten Tokens und das Profil
     * @throws ApiException wenn die Zugangsdaten ungültig sind
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserRow user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        String hash = jdbcTemplate.query("""
                SELECT password_hash FROM user_credentials WHERE user_id = ?
                """, (rs, rowNum) -> rs.getString("password_hash"), user.id())
                .stream()
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Password login is not enabled"));
        if (!passwordEncoder.matches(request.password(), hash)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return issueTokens(user, false);
    }

    /**
     * Stellt mit einem gültigen Refresh-Token neue Tokens aus und macht das
     * bisherige Refresh-Token ungültig (Rotation).
     *
     * @param request das vorhandene Refresh-Token
     * @return die erneuerten Tokens und das Profil
     * @throws ApiException wenn das Refresh-Token ungültig oder abgelaufen ist
     */
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        String tokenHash = hashingService.sha256(request.refreshToken());
        UUID userId = jdbcTemplate.query("""
                SELECT user_id FROM refresh_tokens
                WHERE token_hash = ? AND revoked_at IS NULL AND expires_at > now()
                """, (rs, rowNum) -> rs.getObject("user_id", UUID.class), tokenHash)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        jdbcTemplate.update("UPDATE refresh_tokens SET revoked_at = now() WHERE token_hash = ?", tokenHash);
        UserRow user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
        return issueTokens(user, false);
    }

    /**
     * Macht das angegebene Refresh-Token ungültig (Abmeldung).
     *
     * @param request das abzumeldende Refresh-Token
     */
    @Transactional
    public void logout(RefreshTokenRequest request) {
        jdbcTemplate.update("UPDATE refresh_tokens SET revoked_at = now() WHERE token_hash = ?",
                hashingService.sha256(request.refreshToken()));
    }

    /**
     * Meldet einen Benutzer anhand eines Google-ID-Tokens an.
     *
     * @param request das Google-ID-Token
     * @return die ausgestellten Tokens und das Profil
     */
    @Transactional
    public AuthResponse google(GoogleLoginRequest request) {
        return googleIdToken(request.idToken());
    }

    /**
     * Tauscht einen Google-Autorisierungscode gegen ein ID-Token und meldet
     * den Benutzer damit an.
     *
     * @param code        der Autorisierungscode von Google
     * @param redirectUri die bei Google hinterlegte Rückleitungsadresse
     * @return die ausgestellten Tokens und das Profil
     */
    @Transactional
    public AuthResponse googleAuthorizationCode(String code, String redirectUri) {
        return googleIdToken(exchangeGoogleCode(code, redirectUri));
    }

    /**
     * Prüft ein Google-ID-Token, legt bei Bedarf ein Konto an bzw. verknüpft
     * es und stellt anschließend Tokens aus.
     *
     * @param idToken das zu prüfende Google-ID-Token
     * @return die ausgestellten Tokens und das Profil
     */
    private AuthResponse googleIdToken(String idToken) {
        GoogleIdToken.Payload payload = verifyGoogleIdToken(idToken);
        String subject = payload.getSubject();
        String email = String.valueOf(payload.getEmail());
        String username = deriveUsername(email);

        UUID userId = jdbcTemplate.query("""
                SELECT user_id FROM oauth_accounts WHERE provider = 'google' AND provider_subject = ?
                """, (rs, rowNum) -> rs.getObject("user_id", UUID.class), subject).stream().findFirst().orElse(null);
        UserRow user;
        boolean newUser = false;
        if (userId == null) {
            // Ein neues Konto entsteht nur, wenn es noch keinen Benutzer mit
            // dieser E-Mail gibt (sonst wird Google nur verknüpft).
            java.util.Optional<UserRow> existing = userRepository.findByEmail(email);
            newUser = existing.isEmpty();
            user = existing.orElseGet(() -> userRepository.create(username, email));
            jdbcTemplate.update("""
                    INSERT INTO oauth_accounts (user_id, provider, provider_subject, email)
                    VALUES (?, 'google', ?, ?)
                    ON CONFLICT (provider, provider_subject) DO NOTHING
                    """, user.id(), subject, email);
            jdbcTemplate.update("INSERT INTO avatar_profiles (user_id) VALUES (?) ON CONFLICT DO NOTHING", user.id());
        } else {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Google account is not linked"));
        }
        return issueTokens(user, newUser);
    }

    /**
     * Zieht dem Benutzer Gold ab (z.&nbsp;B. ein Kauf im Avatar-Shop).
     *
     * @param userId die Kennung des Benutzers
     * @param amount der abzuziehende Betrag
     * @return das aktualisierte Profil
     * @throws ApiException wenn der Betrag ungültig ist oder das Gold nicht
     *                      ausreicht
     */
    @Transactional
    public UserProfile spendGold(UUID userId, int amount) {
        if (amount <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Amount must be positive");
        }
        if (!userRepository.spendGold(userId, amount)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Not enough gold");
        }
        return me(userId);
    }

    /**
     * Lädt das Profil eines Benutzers anhand seiner Kennung.
     *
     * @param userId die Kennung des Benutzers
     * @return das Profil des Benutzers
     * @throws ApiException wenn kein Benutzer mit dieser Kennung existiert
     */
    public UserProfile me(UUID userId) {
        return userMapper.toProfile(userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found")));
    }

    /**
     * Speichert das Avatar-Aussehen eines Benutzers (legt es bei Bedarf an).
     *
     * @param userId  die Kennung des Benutzers
     * @param request die neuen Avatar-Merkmale
     * @return das aktualisierte Profil des Benutzers
     */
    @Transactional
    public UserProfile updateAvatar(UUID userId, UpdateAvatarProfileRequest request) {
        jdbcTemplate.update("""
                INSERT INTO avatar_profiles (user_id, body_size, shirt_color, skin_color, hair_color, hair_bangs, hair_style, extra)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (user_id) DO UPDATE SET
                    body_size = excluded.body_size,
                    shirt_color = excluded.shirt_color,
                    skin_color = excluded.skin_color,
                    hair_color = excluded.hair_color,
                    hair_bangs = excluded.hair_bangs,
                    hair_style = excluded.hair_style,
                    extra = excluded.extra
                """, userId, request.bodySize(), request.shirtColor(), request.skinColor(), request.hairColor(),
                request.hairBangs(), request.hairStyle(), request.extra());
        return me(userId);
    }

    /**
     * Erstellt Access- und Refresh-Token für einen Benutzer und speichert das
     * Refresh-Token (als Hash) in der Datenbank.
     *
     * @param user    die Datenbankzeile des Benutzers
     * @param newUser {@code true}, wenn das Konto gerade neu angelegt wurde
     * @return die ausgestellten Tokens samt Profil
     */
    private AuthResponse issueTokens(UserRow user, boolean newUser) {
        String accessToken = jwtService.createAccessToken(user.id(), user.username());
        String refreshToken = hashingService.newOpaqueToken();
        // Der PostgreSQL-JDBC-Treiber kann java.time.Instant nicht direkt binden
        // (SQLState 07006 -> BadSqlGrammarException), daher OffsetDateTime verwenden.
        OffsetDateTime expiresAt = OffsetDateTime.ofInstant(
                Instant.now().plus(jwtProperties.refreshTokenDays(), ChronoUnit.DAYS), ZoneOffset.UTC);
        jdbcTemplate.update("""
                INSERT INTO refresh_tokens (user_id, token_hash, expires_at)
                VALUES (?, ?, ?)
                """, user.id(), hashingService.sha256(refreshToken), expiresAt);
        return new AuthResponse(accessToken, refreshToken, userMapper.toProfile(user), newUser);
    }

    /**
     * Prüft die Signatur und Gültigkeit eines Google-ID-Tokens.
     *
     * @param idToken das zu prüfende ID-Token
     * @return die enthaltenen Nutzdaten (z.&nbsp;B. E-Mail, Subjekt)
     * @throws ApiException wenn Google nicht konfiguriert oder das Token
     *                      ungültig ist
     */
    private GoogleIdToken.Payload verifyGoogleIdToken(String idToken) {
        if (googleProperties.clientId() == null || googleProperties.clientId().isBlank()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Google login is not configured");
        }
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleProperties.clientId()))
                    .build();
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid Google token");
            }
            return token.getPayload();
        } catch (GeneralSecurityException | IOException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Google token verification failed");
        }
    }

    /**
     * Tauscht einen Google-Autorisierungscode bei Google gegen ein ID-Token.
     *
     * @param code        der Autorisierungscode
     * @param redirectUri die bei Google hinterlegte Rückleitungsadresse
     * @return das von Google zurückgegebene ID-Token
     * @throws ApiException wenn Google nicht konfiguriert ist oder der Tausch
     *                      fehlschlägt
     */
    private String exchangeGoogleCode(String code, String redirectUri) {
        if (googleProperties.clientId() == null || googleProperties.clientId().isBlank()
                || googleProperties.clientSecret() == null || googleProperties.clientSecret().isBlank()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Server Google OAuth is not configured. Set GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET on the server.");
        }
        try {
            String body = "client_id=" + urlEncode(googleProperties.clientId())
                    + "&client_secret=" + urlEncode(googleProperties.clientSecret())
                    + "&code=" + urlEncode(code)
                    + "&grant_type=authorization_code"
                    + "&redirect_uri=" + urlEncode(redirectUri);
            HttpRequest request = HttpRequest.newBuilder(URI.create(GOOGLE_TOKEN_URL))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Google token exchange failed: " + response.body());
            }
            JsonNode idToken = objectMapper.readTree(response.body()).get("id_token");
            if (idToken == null || idToken.asText().isBlank()) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Google did not return an ID token");
            }
            return idToken.asText();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Google token exchange interrupted");
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Google token exchange failed");
        }
    }

    /**
     * Kodiert einen Wert für die Verwendung in einer URL bzw. einem
     * Formular-Body.
     *
     * @param value der zu kodierende Wert
     * @return der URL-kodierte Wert
     */
    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Leitet aus einer E-Mail-Adresse einen freien, gültigen Benutzernamen ab
     * und hängt bei Bedarf eine Zahl an, um Eindeutigkeit sicherzustellen.
     *
     * @param email die E-Mail-Adresse des Benutzers
     * @return ein noch nicht vergebener Benutzername
     */
    private String deriveUsername(String email) {
        String base = email == null ? "player" : email.split("@")[0].replaceAll("[^A-Za-z0-9_]", "");
        if (base.length() < 3) {
            base = "player";
        }
        String candidate = base.substring(0, Math.min(base.length(), 24));
        int suffix = 1;
        while (userRepository.findByUsernameOrEmail(candidate).isPresent()) {
            candidate = base.substring(0, Math.min(base.length(), 20)) + suffix++;
        }
        return candidate;
    }
}
