package com.disciplica.shared.user;

public record AvatarProfileDto(
        String bodySize,
        String shirtColor,
        String skinColor,
        String hairColor,
        String hairBangs,
        String hairStyle,
        String extra
) {
}
