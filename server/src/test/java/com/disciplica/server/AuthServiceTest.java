package com.disciplica.server;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.disciplica.server.auth.AuthService;
import com.disciplica.server.support.ApiException;
import com.disciplica.shared.auth.AuthResponse;
import com.disciplica.shared.auth.LoginRequest;
import com.disciplica.shared.auth.RefreshTokenRequest;
import com.disciplica.shared.auth.RegisterRequest;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {
    @Autowired
    private AuthService authService;

    @Test
    void registersLogsInAndRotatesRefreshTokens() {
        AuthResponse registered = authService.register(new RegisterRequest(
                "authhero",
                "authhero@example.com",
                "very-secure-password"
        ));

        AuthResponse loggedIn = authService.login(new LoginRequest(
                "authhero@example.com",
                "very-secure-password"
        ));

        AuthResponse refreshed = authService.refresh(new RefreshTokenRequest(loggedIn.refreshToken()));

        assertNotNull(registered.accessToken());
        assertNotNull(loggedIn.accessToken());
        assertNotNull(refreshed.accessToken());
        assertNotEquals(loggedIn.refreshToken(), refreshed.refreshToken());
        assertThrows(ApiException.class, () -> authService.refresh(new RefreshTokenRequest(loggedIn.refreshToken())));
    }
}
