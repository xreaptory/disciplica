package com.disciplica.shared.party;

import java.time.Instant;
import java.util.UUID;

public record ChatMessageDto(
        UUID id,
        UUID partyId,
        UUID senderId,
        String senderUsername,
        String message,
        Instant createdAt
) {
}
