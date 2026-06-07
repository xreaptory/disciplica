package com.disciplica.shared.party;

import java.util.List;
import java.util.UUID;

public record PartyDto(
        UUID id,
        String name,
        UUID leaderId,
        List<PartyMemberDto> members
) {
}
