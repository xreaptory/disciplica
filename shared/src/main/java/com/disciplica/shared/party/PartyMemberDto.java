package com.disciplica.shared.party;

import java.time.Instant;
import java.util.UUID;

public record PartyMemberDto(
        UUID userId,
        String username,
        String role,
        Instant joinedAt
) {
}
