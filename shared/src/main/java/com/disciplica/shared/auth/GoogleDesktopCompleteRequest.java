package com.disciplica.shared.auth;

import jakarta.validation.constraints.NotBlank;

public record GoogleDesktopCompleteRequest(
        @NotBlank String code
) {
}
