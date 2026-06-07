package View.api;

import com.disciplica.shared.user.UserProfile;

public class SessionStore {
    private ApiClient apiClient;
    private UserProfile userProfile;

    public ApiClient apiClient() {
        return apiClient;
    }

    public UserProfile userProfile() {
        return userProfile;
    }

    public void authenticate(ApiClient apiClient, UserProfile userProfile) {
        this.apiClient = apiClient;
        this.userProfile = userProfile;
    }

    public boolean isAuthenticated() {
        return apiClient != null && userProfile != null;
    }
}
