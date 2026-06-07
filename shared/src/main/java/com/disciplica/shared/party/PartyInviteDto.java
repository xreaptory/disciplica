package com.disciplica.shared.party;

import java.time.Instant;
import java.util.UUID;

public record PartyInviteDto(
        UUID id,
        UUID partyId,
        String partyName,
        UUID invitedUserId,
        String status,
        Instant createdAt
) {
}
