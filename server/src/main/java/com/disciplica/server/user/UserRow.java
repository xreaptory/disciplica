package com.disciplica.server.user;

import java.util.UUID;

public record UserRow(
        UUID id,
        String username,
        String email,
        int level,
        int xp,
        int health,
        int gold
) {
}
