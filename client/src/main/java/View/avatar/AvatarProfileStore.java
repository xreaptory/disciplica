package View.avatar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class AvatarProfileStore {
    private static final Path AVATAR_PATH = Path.of("data", "avatar-profile.properties");

    public AvatarState load() {
        AvatarState state = AvatarState.defaults();
        if (!Files.exists(AVATAR_PATH)) {
            return state;
        }
        Properties properties = new Properties();
        try (InputStream stream = Files.newInputStream(AVATAR_PATH)) {
            properties.load(stream);
            state.setBodySize(properties.getProperty("bodySize"));
            state.setShirtColor(properties.getProperty("shirtColor"));
            state.setSkinColor(properties.getProperty("skinColor"));
            state.setHairColor(properties.getProperty("hairColor"));
            state.setHairBangs(properties.getProperty("hairBangs"));
            state.setHairStyle(properties.getProperty("hairStyle"));
            state.setExtra(properties.getProperty("extra"));
        } catch (IOException ignored) {
        }
        return state;
    }

    public void save(AvatarState state) throws IOException {
        Files.createDirectories(AVATAR_PATH.getParent());
        Properties properties = new Properties();
        properties.setProperty("bodySize", state.getBodySize());
        properties.setProperty("shirtColor", state.getShirtColor());
        properties.setProperty("skinColor", state.getSkinColor());
        properties.setProperty("hairColor", state.getHairColor());
        properties.setProperty("hairBangs", state.getHairBangs());
        properties.setProperty("hairStyle", state.getHairStyle());
        properties.setProperty("extra", state.getExtra());
        try (OutputStream stream = Files.newOutputStream(AVATAR_PATH)) {
            properties.store(stream, "Disciplica avatar profile");
        }
    }
}
