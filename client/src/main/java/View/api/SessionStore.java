package View.api;

import com.disciplica.shared.user.UserProfile;
import View.avatar.AvatarProfileStore;
import View.avatar.AvatarState;

public class SessionStore {
    private final AvatarProfileStore avatarProfileStore = new AvatarProfileStore();
    private ApiClient apiClient;
    private UserProfile userProfile;
    private AvatarState avatarState = avatarProfileStore.load();

    public ApiClient apiClient() {
        return apiClient;
    }

    public UserProfile userProfile() {
        return userProfile;
    }

    public AvatarState avatarState() {
        return avatarState;
    }

    public void authenticate(ApiClient apiClient, UserProfile userProfile) {
        this.apiClient = apiClient;
        this.userProfile = userProfile;
        this.avatarState = AvatarState.fromDto(userProfile.avatar());
    }

    public boolean isAuthenticated() {
        return apiClient != null && userProfile != null;
    }

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
