package com.disciplica.shared.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAvatarProfileRequest(
        @NotBlank @Size(max = 32) String bodySize,
        @NotBlank @Size(max = 32) String shirtColor,
        @NotBlank @Size(max = 32) String skinColor,
        @NotBlank @Size(max = 32) String hairColor,
        @NotBlank @Size(max = 32) String hairBangs,
        @NotBlank @Size(max = 32) String hairStyle,
        @NotBlank @Size(max = 32) String extra
) {
}
