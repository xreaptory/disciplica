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
import com.disciplica.shared.user.UpdateAvatarProfileRequest;
import com.disciplica.shared.user.UserProfile;

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

    public AuthController(AuthService authService, CurrentUser currentUser, GoogleProperties googleProperties) {
        this.authService = authService;
        this.currentUser = currentUser;
        this.googleProperties = googleProperties;
    }

    @PostMapping("/auth/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/auth/google")
    public AuthResponse google(@Valid @RequestBody GoogleLoginRequest request) {
        return authService.google(request);
    }

    @GetMapping("/auth/google/desktop/start")
    public ResponseEntity<?> googleDesktopStart(@RequestParam String appRedirectUri) {
        if (!isDesktopGoogleOAuthConfigured()) {
            // Return JSON directly — avoid any exception/handler path that could produce a 500.
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

    @GetMapping("/auth/google/desktop/callback")
    public ResponseEntity<Void> googleDesktopCallback(@RequestParam String code, @RequestParam String state) {
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

    @PostMapping("/auth/google/desktop/complete")
    public AuthResponse googleDesktopComplete(@Valid @RequestBody GoogleDesktopCompleteRequest request) {
        cleanupDesktopOAuth();
        DesktopOAuthToken token = desktopTokens.remove(request.code());
        if (token == null || token.expiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Google desktop login code expired");
        }
        return token.authResponse();
    }

    @PostMapping("/auth/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/auth/logout")
    public void logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
    }

    @GetMapping("/me")
    public UserProfile me(Authentication authentication) {
        UUID userId = currentUser.requireUserId(authentication);
        return authService.me(userId);
    }

    @PatchMapping("/me/avatar")
    public UserProfile updateAvatar(Authentication authentication, @Valid @RequestBody UpdateAvatarProfileRequest request) {
        UUID userId = currentUser.requireUserId(authentication);
        return authService.updateAvatar(userId, request);
    }

    private ResponseEntity<?> redirect(String location) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(location));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    private String serverGoogleRedirectUri() {
        String baseUrl = googleProperties.publicBaseUrl() == null || googleProperties.publicBaseUrl().isBlank()
                ? "http://localhost:8080"
                : googleProperties.publicBaseUrl();
        return baseUrl.replaceAll("/+$", "") + "/auth/google/desktop/callback";
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private boolean isDesktopGoogleOAuthConfigured() {
        return googleProperties.clientId() != null && !googleProperties.clientId().isBlank()
                && googleProperties.clientSecret() != null && !googleProperties.clientSecret().isBlank();
    }

    private void cleanupDesktopOAuth() {
        Instant now = Instant.now();
        desktopRequests.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
        desktopTokens.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
    }

    private record DesktopOAuthRequest(String appRedirectUri, Instant expiresAt) {
    }

    private record DesktopOAuthToken(AuthResponse authResponse, Instant expiresAt) {
    }
}
