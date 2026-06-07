package com.disciplica.shared.auth;

import com.disciplica.shared.user.UserProfile;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserProfile user
) {
}
