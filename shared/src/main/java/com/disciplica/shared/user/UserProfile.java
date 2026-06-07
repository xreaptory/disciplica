package com.disciplica.shared.user;

import java.util.UUID;

public record UserProfile(
        UUID id,
        String username,
        String email,
        int level,
        int xp,
        int health,
        int gold,
        AvatarProfileDto avatar
) {
}
