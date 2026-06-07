package View.api;

import com.disciplica.shared.auth.AuthResponse;
import com.disciplica.shared.auth.GoogleDesktopCompleteRequest;
import com.disciplica.shared.auth.GoogleLoginRequest;
import com.disciplica.shared.auth.LoginRequest;
import com.disciplica.shared.auth.RegisterRequest;
import com.disciplica.shared.party.ChatMessageDto;
import com.disciplica.shared.party.CreatePartyRequest;
import com.disciplica.shared.party.InvitePartyRequest;
import com.disciplica.shared.party.PartyDto;
import com.disciplica.shared.party.PartyInviteDto;
import com.disciplica.shared.party.SendChatMessageRequest;
import com.disciplica.shared.task.CreateTaskRequest;
import com.disciplica.shared.task.TaskDto;
import com.disciplica.shared.task.UpdateTaskRequest;
import com.disciplica.shared.user.UserProfile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

public class ApiClient {
    private final URI baseUri;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String accessToken;
    private String refreshToken;

    public ApiClient(String baseUrl) {
        this.baseUri = URI.create(baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl);
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public AuthResponse register(RegisterRequest request) {
        return post("/auth/register", request, AuthResponse.class, false);
    }

    public AuthResponse login(LoginRequest request) {
        return post("/auth/login", request, AuthResponse.class, false);
    }

    public AuthResponse google(String idToken) {
        return post("/auth/google", new GoogleLoginRequest(idToken), AuthResponse.class, false);
    }

    public AuthResponse googleDesktopComplete(String code) {
        return post("/auth/google/desktop/complete", new GoogleDesktopCompleteRequest(code), AuthResponse.class, false);
    }

    public UserProfile me() {
        return get("/me", UserProfile.class);
    }

    public List<TaskDto> tasks() {
        return get("/tasks", new TypeReference<>() {
        });
    }

    public TaskDto createTask(CreateTaskRequest request) {
        return post("/tasks", request, TaskDto.class, true);
    }

    public TaskDto updateTask(UUID taskId, UpdateTaskRequest request) {
        return patch("/tasks/" + taskId, request, TaskDto.class);
    }

    public TaskDto completeTask(UUID taskId) {
        return post("/tasks/" + taskId + "/complete", null, TaskDto.class, true);
    }

    public void deleteTask(UUID taskId) {
        send(request("/tasks/" + taskId).DELETE().build(), Void.class);
    }

    public PartyDto createParty(String name) {
        return post("/parties", new CreatePartyRequest(name), PartyDto.class, true);
    }

    public PartyDto currentParty() {
        return get("/parties/current", PartyDto.class);
    }

    public PartyInviteDto invite(String usernameOrEmail) {
        return post("/parties/current/invites", new InvitePartyRequest(usernameOrEmail), PartyInviteDto.class, true);
    }

    public List<ChatMessageDto> partyMessages() {
        return get("/parties/current/messages", new TypeReference<>() {
        });
    }

    public ChatMessageDto sendPartyMessage(String message) {
        return post("/parties/current/messages", new SendChatMessageRequest(message), ChatMessageDto.class, true);
    }

    public void store(AuthResponse response) {
        this.accessToken = response.accessToken();
        this.refreshToken = response.refreshToken();
    }

    public String accessToken() {
        return accessToken;
    }

    public String refreshToken() {
        return refreshToken;
    }

    private <T> T get(String path, Class<T> responseType) {
        return send(request(path).GET().build(), responseType);
    }

    private <T> T get(String path, TypeReference<T> responseType) {
        return send(request(path).GET().build(), responseType);
    }

    private <T> T post(String path, Object body, Class<T> responseType, boolean authenticated) {
        HttpRequest.Builder builder = authenticated ? request(path) : unauthenticatedRequest(path);
        return send(builder.POST(bodyPublisher(body)).build(), responseType);
    }

    private <T> T patch(String path, Object body, Class<T> responseType) {
        return send(request(path).method("PATCH", bodyPublisher(body)).build(), responseType);
    }

    private HttpRequest.Builder request(String path) {
        HttpRequest.Builder builder = unauthenticatedRequest(path);
        if (accessToken != null && !accessToken.isBlank()) {
            builder.header("Authorization", "Bearer " + accessToken);
        }
        return builder;
    }

    private HttpRequest.Builder unauthenticatedRequest(String path) {
        return HttpRequest.newBuilder(baseUri.resolve(path))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
    }

    private HttpRequest.BodyPublisher bodyPublisher(Object body) {
        try {
            return HttpRequest.BodyPublishers.ofString(body == null ? "" : objectMapper.writeValueAsString(body));
        } catch (IOException exception) {
            throw new ApiClientException("Failed to serialize request", exception);
        }
    }

    private <T> T send(HttpRequest request, Class<T> responseType) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            ensureSuccess(response);
            if (responseType == Void.class || response.body().isBlank()) {
                return null;
            }
            return objectMapper.readValue(response.body(), responseType);
        } catch (IOException exception) {
            throw new ApiClientException("Server request failed", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiClientException("Server request interrupted", exception);
        }
    }

    private <T> T send(HttpRequest request, TypeReference<T> responseType) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            ensureSuccess(response);
            return objectMapper.readValue(response.body(), responseType);
        } catch (IOException exception) {
            throw new ApiClientException("Server request failed", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiClientException("Server request interrupted", exception);
        }
    }

    private void ensureSuccess(HttpResponse<String> response) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new ApiClientException("Server returned " + response.statusCode() + ": " + response.body());
        }
    }
}
