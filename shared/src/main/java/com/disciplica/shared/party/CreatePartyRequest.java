package com.disciplica.shared.party;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePartyRequest(
        @NotBlank @Size(max = 80) String name
) {
}
