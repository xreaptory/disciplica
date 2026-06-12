package View.api;

import com.disciplica.shared.user.UserProfile;
import View.avatar.AvatarProfileStore;
import View.avatar.AvatarState;

/**
 * Hält die Daten der aktuellen Sitzung zusammen.
 * <p>
 * Speichert den angemeldeten {@link ApiClient}, das Benutzerprofil und den
 * Avatar-Zustand. Im angemeldeten Zustand werden Avatar-Änderungen an den
 * Server gesendet, sonst lokal gespeichert.
 */
public class SessionStore {
    private final AvatarProfileStore avatarProfileStore = new AvatarProfileStore();
    private ApiClient apiClient;
    private UserProfile userProfile;
    private AvatarState avatarState = avatarProfileStore.load();

    /**
     * {@return der API-Client der aktuellen Sitzung oder {@code null}, wenn
     * nicht angemeldet}
     */
    public ApiClient apiClient() {
        return apiClient;
    }

    /**
     * {@return das Profil des angemeldeten Benutzers oder {@code null}}
     */
    public UserProfile userProfile() {
        return userProfile;
    }

    /**
     * {@return der aktuelle Avatar-Zustand}
     */
    public AvatarState avatarState() {
        return avatarState;
    }

    /**
     * Übernimmt die Daten einer erfolgreichen Anmeldung in die Sitzung.
     *
     * @param apiClient   der angemeldete API-Client
     * @param userProfile das Profil des Benutzers
     */
    public void authenticate(ApiClient apiClient, UserProfile userProfile) {
        this.apiClient = apiClient;
        this.userProfile = userProfile;
        this.avatarState = AvatarState.fromDto(userProfile.avatar());
    }

    /**
     * Setzt die Sitzung zurück (Abmeldung): API-Client und Profil werden
     * verworfen, sodass kein angemeldeter Zustand mehr vorliegt.
     */
    public void clear() {
        this.apiClient = null;
        this.userProfile = null;
    }

    /**
     * {@return {@code true}, wenn ein Benutzer angemeldet ist}
     */
    public boolean isAuthenticated() {
        return apiClient != null && userProfile != null;
    }

    /**
     * Aktualisiert den Avatar. Im angemeldeten Zustand wird die Änderung an
     * den Server gesendet, andernfalls lokal gespeichert.
     *
     * @param avatarState der neue Avatar-Zustand
     * @throws ApiClientException wenn der Avatar lokal nicht gespeichert werden
     *                            kann
     */
    public void updateAvatar(AvatarState avatarState) {
        this.avatarState = avatarState;
        if (isAuthenticated()) {
            userProfile = apiClient.updateAvatar(avatarState.toUpdateRequest());
            return;
        }
        try {
            avatarProfileStore.save(avatarState);
        } catch (java.io.IOException exception) {
            throw new ApiClientException("Avatar was updated but could not be saved locally", exception);
        }
    }
}
