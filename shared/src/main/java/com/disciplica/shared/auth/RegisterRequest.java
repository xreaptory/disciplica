package com.disciplica.shared.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 32) String username,
        @Email @NotBlank String email,
        @NotBlank @Size(min = 10, max = 128) String password
) {
}
