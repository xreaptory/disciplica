package com.disciplica.shared.party;

import jakarta.validation.constraints.NotBlank;

public record InvitePartyRequest(
        @NotBlank String usernameOrEmail
) {
}
