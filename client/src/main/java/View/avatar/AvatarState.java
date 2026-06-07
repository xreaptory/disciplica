package View.avatar;

import com.disciplica.shared.user.AvatarProfileDto;
import com.disciplica.shared.user.UpdateAvatarProfileRequest;

public class AvatarState {
    private String bodySize = "Medium";
    private String shirtColor = "Blue";
    private String skinColor = "Warm";
    private String hairColor = "Brown";
    private String hairBangs = "None";
    private String hairStyle = "Short";
    private String extra = "None";

    public static AvatarState defaults() {
        return new AvatarState();
    }

    public static AvatarState fromDto(AvatarProfileDto dto) {
        AvatarState state = defaults();
        if (dto == null) {
            return state;
        }
        state.setBodySize(display(dto.bodySize(), state.bodySize));
        state.setShirtColor(display(dto.shirtColor(), state.shirtColor));
        state.setSkinColor(display(dto.skinColor(), state.skinColor));
        state.setHairColor(display(dto.hairColor(), state.hairColor));
        state.setHairBangs(display(dto.hairBangs(), state.hairBangs));
        state.setHairStyle(display(dto.hairStyle(), state.hairStyle));
        state.setExtra(display(dto.extra(), state.extra));
        return state;
    }

    public AvatarProfileDto toDto() {
        return new AvatarProfileDto(
                value(bodySize),
                value(shirtColor),
                value(skinColor),
                value(hairColor),
                value(hairBangs),
                value(hairStyle),
                value(extra)
        );
    }

    public UpdateAvatarProfileRequest toUpdateRequest() {
        return new UpdateAvatarProfileRequest(
                value(bodySize),
                value(shirtColor),
                value(skinColor),
                value(hairColor),
                value(hairBangs),
                value(hairStyle),
                value(extra)
        );
    }

    public String getBodySize() {
        return bodySize;
    }

    public void setBodySize(String bodySize) {
        this.bodySize = safe(bodySize, "Medium");
    }

    public String getShirtColor() {
        return shirtColor;
    }

    public void setShirtColor(String shirtColor) {
        this.shirtColor = safe(shirtColor, "Blue");
    }

    public String getSkinColor() {
        return skinColor;
    }

    public void setSkinColor(String skinColor) {
        this.skinColor = safe(skinColor, "Warm");
    }

    public String getHairColor() {
        return hairColor;
    }

    public void setHairColor(String hairColor) {
        this.hairColor = safe(hairColor, "Brown");
    }

    public String getHairBangs() {
        return hairBangs;
    }

    public void setHairBangs(String hairBangs) {
        this.hairBangs = safe(hairBangs, "None");
    }

    public String getHairStyle() {
        return hairStyle;
    }

    public void setHairStyle(String hairStyle) {
        this.hairStyle = safe(hairStyle, "Short");
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = safe(extra, "None");
    }

    private static String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String value(String display) {
        return display == null ? "" : display.trim().toLowerCase().replace(' ', '-');
    }

    private static String display(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String normalized = value.replace('-', ' ').replace('_', ' ').trim();
        StringBuilder builder = new StringBuilder();
        for (String part : normalized.split("\\s+")) {
            if (!part.isBlank()) {
                builder.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    builder.append(part.substring(1).toLowerCase());
                }
                builder.append(' ');
            }
        }
        return builder.toString().trim();
    }
}
