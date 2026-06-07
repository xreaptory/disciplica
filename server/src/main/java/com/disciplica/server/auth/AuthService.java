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

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        try {
            UserRow user = userRepository.create(request.username(), request.email());
            jdbcTemplate.update("INSERT INTO user_credentials (user_id, password_hash) VALUES (?, ?)",
                    user.id(), passwordEncoder.encode(request.password()));
            jdbcTemplate.update("INSERT INTO avatar_profiles (user_id) VALUES (?)", user.id());
            return issueTokens(user);
        } catch (DuplicateKeyException exception) {
            throw new ApiException(HttpStatus.CONFLICT, "Username or email already exists");
        }
    }

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
        return issueTokens(user);
    }

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
        return issueTokens(user);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        jdbcTemplate.update("UPDATE refresh_tokens SET revoked_at = now() WHERE token_hash = ?",
                hashingService.sha256(request.refreshToken()));
    }

    @Transactional
    public AuthResponse google(GoogleLoginRequest request) {
        return googleIdToken(request.idToken());
    }

    @Transactional
    public AuthResponse googleAuthorizationCode(String code, String redirectUri) {
        return googleIdToken(exchangeGoogleCode(code, redirectUri));
    }

    private AuthResponse googleIdToken(String idToken) {
        GoogleIdToken.Payload payload = verifyGoogleIdToken(idToken);
        String subject = payload.getSubject();
        String email = String.valueOf(payload.getEmail());
        String username = deriveUsername(email);

        UUID userId = jdbcTemplate.query("""
                SELECT user_id FROM oauth_accounts WHERE provider = 'google' AND provider_subject = ?
                """, (rs, rowNum) -> rs.getObject("user_id", UUID.class), subject).stream().findFirst().orElse(null);
        UserRow user;
        if (userId == null) {
            user = userRepository.findByEmail(email).orElseGet(() -> userRepository.create(username, email));
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
        return issueTokens(user);
    }

    public UserProfile me(UUID userId) {
        return userMapper.toProfile(userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found")));
    }

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

    private AuthResponse issueTokens(UserRow user) {
        String accessToken = jwtService.createAccessToken(user.id(), user.username());
        String refreshToken = hashingService.newOpaqueToken();
        jdbcTemplate.update("""
                INSERT INTO refresh_tokens (user_id, token_hash, expires_at)
                VALUES (?, ?, ?)
                """, user.id(), hashingService.sha256(refreshToken),
                Instant.now().plus(jwtProperties.refreshTokenDays(), ChronoUnit.DAYS));
        return new AuthResponse(accessToken, refreshToken, userMapper.toProfile(user));
    }

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

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

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
