package View.api;

import com.disciplica.shared.auth.AuthResponse;
import com.disciplica.shared.auth.GoogleDesktopCompleteRequest;
import com.disciplica.shared.auth.GoogleLoginRequest;
import com.disciplica.shared.auth.LoginRequest;
import com.disciplica.shared.auth.RefreshTokenRequest;
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
import com.disciplica.shared.user.UpdateAvatarProfileRequest;
import com.disciplica.shared.user.UserProfile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * HTTP-Client für die Kommunikation des Clients mit dem Server.
 * <p>
 * Kapselt alle REST-Aufrufe (Anmeldung, Aufgaben, Gruppen, Avatar) als
 * Java-Methoden, kümmert sich um die JSON-Umwandlung und hängt bei
 * geschützten Aufrufen das Access-Token als {@code Authorization}-Header an.
 */
public class ApiClient {
    // Der kostenlose Render-Tarif fährt inaktive Instanzen herunter; der erste
    // Aufruf kann daher abbrechen, während der Server wieder hochfährt. Ein
    // großzügiges Zeitlimit plus ein erneuter Versuch fangen diesen Kaltstart ab.
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final int MAX_ATTEMPTS = 2;
    private static final Duration RETRY_BACKOFF = Duration.ofMillis(1500);

    private final URI baseUri;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String accessToken;
    private String refreshToken;

    /**
     * Erzeugt den Client für eine bestimmte Server-Adresse.
     *
     * @param baseUrl die Basis-Adresse des Servers
     */
    public ApiClient(String baseUrl) {
        this.baseUri = URI.create(baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl);
        this.httpClient = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    /**
     * Registriert ein neues Benutzerkonto.
     *
     * @param request die Registrierungsdaten
     * @return die Anmeldeantwort mit Tokens und Profil
     */
    public AuthResponse register(RegisterRequest request) {
        return post("/auth/register", request, AuthResponse.class, false);
    }

    /**
     * Meldet einen Benutzer mit E-Mail und Passwort an.
     *
     * @param request die Anmeldedaten
     * @return die Anmeldeantwort mit Tokens und Profil
     */
    public AuthResponse login(LoginRequest request) {
        return post("/auth/login", request, AuthResponse.class, false);
    }

    /**
     * Meldet einen Benutzer über ein Google-ID-Token an.
     *
     * @param idToken das Google-ID-Token
     * @return die Anmeldeantwort mit Tokens und Profil
     */
    public AuthResponse google(String idToken) {
        return post("/auth/google", new GoogleLoginRequest(idToken), AuthResponse.class, false);
    }

    /**
     * Schließt die Google-Anmeldung der Desktop-Anwendung mit dem
     * Zwischencode ab.
     *
     * @param code der Zwischencode
     * @return die Anmeldeantwort mit Tokens und Profil
     */
    public AuthResponse googleDesktopComplete(String code) {
        return post("/auth/google/desktop/complete", new GoogleDesktopCompleteRequest(code), AuthResponse.class, false);
    }

    /**
     * {@return das Profil des angemeldeten Benutzers}
     */
    public UserProfile me() {
        return get("/me", UserProfile.class);
    }

    /**
     * Aktualisiert das Avatar-Aussehen des angemeldeten Benutzers.
     *
     * @param request die neuen Avatar-Merkmale
     * @return das aktualisierte Profil
     */
    public UserProfile updateAvatar(UpdateAvatarProfileRequest request) {
        return patch("/me/avatar", request, UserProfile.class);
    }

    /**
     * {@return alle Aufgaben des angemeldeten Benutzers}
     */
    public List<TaskDto> tasks() {
        return get("/tasks", new TypeReference<>() {
        });
    }

    /**
     * Legt eine neue Aufgabe an.
     *
     * @param request die Daten der neuen Aufgabe
     * @return die angelegte Aufgabe
     */
    public TaskDto createTask(CreateTaskRequest request) {
        return post("/tasks", request, TaskDto.class, true);
    }

    /**
     * Ändert eine bestehende Aufgabe.
     *
     * @param taskId  die Kennung der Aufgabe
     * @param request die zu ändernden Felder
     * @return die aktualisierte Aufgabe
     */
    public TaskDto updateTask(UUID taskId, UpdateTaskRequest request) {
        return patch("/tasks/" + taskId, request, TaskDto.class);
    }

    /**
     * Schließt eine Aufgabe ab.
     *
     * @param taskId die Kennung der Aufgabe
     * @return die abgeschlossene Aufgabe
     */
    public TaskDto completeTask(UUID taskId) {
        return post("/tasks/" + taskId + "/complete", null, TaskDto.class, true);
    }

    /**
     * Löscht eine Aufgabe.
     *
     * @param taskId die Kennung der Aufgabe
     */
    public void deleteTask(UUID taskId) {
        send(() -> request("/tasks/" + taskId).DELETE().build(), Void.class);
    }

    /**
     * Erstellt eine neue Gruppe.
     *
     * @param name der Name der Gruppe
     * @return die erstellte Gruppe
     */
    public PartyDto createParty(String name) {
        return post("/parties", new CreatePartyRequest(name), PartyDto.class, true);
    }

    /**
     * {@return die aktuelle Gruppe des angemeldeten Benutzers}
     */
    public PartyDto currentParty() {
        return get("/parties/current", PartyDto.class);
    }

    /**
     * Lädt einen Benutzer in die aktuelle Gruppe ein.
     *
     * @param usernameOrEmail Benutzername oder E-Mail der einzuladenden Person
     * @return die erstellte Einladung
     */
    public PartyInviteDto invite(String usernameOrEmail) {
        return post("/parties/current/invites", new InvitePartyRequest(usernameOrEmail), PartyInviteDto.class, true);
    }

    /**
     * {@return die offenen Einladungen, die an den angemeldeten Benutzer
     * gerichtet sind}
     */
    public List<PartyInviteDto> pendingInvites() {
        return get("/party-invites", new TypeReference<>() {
        });
    }

    /**
     * Nimmt eine Gruppeneinladung an.
     *
     * @param inviteId die Kennung der Einladung
     * @return die angenommene Einladung
     */
    public PartyInviteDto acceptInvite(UUID inviteId) {
        return post("/party-invites/" + inviteId + "/accept", null, PartyInviteDto.class, true);
    }

    /**
     * Lehnt eine Gruppeneinladung ab.
     *
     * @param inviteId die Kennung der Einladung
     * @return die abgelehnte Einladung
     */
    public PartyInviteDto declineInvite(UUID inviteId) {
        return post("/party-invites/" + inviteId + "/decline", null, PartyInviteDto.class, true);
    }

    /**
     * {@return die Chat-Nachrichten der aktuellen Gruppe}
     */
    public List<ChatMessageDto> partyMessages() {
        return get("/parties/current/messages", new TypeReference<>() {
        });
    }

    /**
     * Sendet eine Chat-Nachricht in die aktuelle Gruppe.
     *
     * @param message der Nachrichtentext
     * @return die gespeicherte Chat-Nachricht
     */
    public ChatMessageDto sendPartyMessage(String message) {
        return post("/parties/current/messages", new SendChatMessageRequest(message), ChatMessageDto.class, true);
    }

    /**
     * Übernimmt die Tokens einer Anmeldeantwort für nachfolgende Aufrufe.
     *
     * @param response die Anmeldeantwort
     */
    public void store(AuthResponse response) {
        this.accessToken = response.accessToken();
        this.refreshToken = response.refreshToken();
    }

    /**
     * {@return das aktuelle Access-Token}
     */
    public String accessToken() {
        return accessToken;
    }

    /**
     * {@return das aktuelle Refresh-Token}
     */
    public String refreshToken() {
        return refreshToken;
    }

    /**
     * Führt einen GET-Aufruf aus und wandelt die Antwort in den angegebenen
     * Typ um.
     *
     * @param path         der Pfad des Endpunkts
     * @param responseType der erwartete Antworttyp
     * @param <T>          der Antworttyp
     * @return die umgewandelte Antwort
     */
    private <T> T get(String path, Class<T> responseType) {
        return send(() -> request(path).GET().build(), responseType);
    }

    /**
     * Wie {@link #get(String, Class)}, jedoch für generische Typen (z.&nbsp;B.
     * Listen).
     *
     * @param path         der Pfad des Endpunkts
     * @param responseType die Typreferenz der erwarteten Antwort
     * @param <T>          der Antworttyp
     * @return die umgewandelte Antwort
     */
    private <T> T get(String path, TypeReference<T> responseType) {
        return send(() -> request(path).GET().build(), responseType);
    }

    /**
     * Führt einen POST-Aufruf aus.
     *
     * @param path          der Pfad des Endpunkts
     * @param body          der zu sendende Rumpf (darf {@code null} sein)
     * @param responseType  der erwartete Antworttyp
     * @param authenticated {@code true}, wenn der Aufruf das Access-Token
     *                      benötigt
     * @param <T>           der Antworttyp
     * @return die umgewandelte Antwort
     */
    private <T> T post(String path, Object body, Class<T> responseType, boolean authenticated) {
        return send(() -> (authenticated ? request(path) : unauthenticatedRequest(path))
                .POST(bodyPublisher(body)).build(), responseType);
    }

    /**
     * Führt einen PATCH-Aufruf aus.
     *
     * @param path         der Pfad des Endpunkts
     * @param body         der zu sendende Rumpf
     * @param responseType der erwartete Antworttyp
     * @param <T>          der Antworttyp
     * @return die umgewandelte Antwort
     */
    private <T> T patch(String path, Object body, Class<T> responseType) {
        return send(() -> request(path).method("PATCH", bodyPublisher(body)).build(), responseType);
    }

    /**
     * Erstellt einen Anfrage-Baukasten und hängt – falls vorhanden – das
     * Access-Token an.
     *
     * @param path der Pfad des Endpunkts
     * @return der vorbereitete Anfrage-Baukasten
     */
    private HttpRequest.Builder request(String path) {
        HttpRequest.Builder builder = unauthenticatedRequest(path);
        if (accessToken != null && !accessToken.isBlank()) {
            builder.header("Authorization", "Bearer " + accessToken);
        }
        return builder;
    }

    /**
     * Erstellt einen Anfrage-Baukasten ohne Anmeldung mit den
     * Standard-Headern.
     *
     * @param path der Pfad des Endpunkts
     * @return der vorbereitete Anfrage-Baukasten
     */
    private HttpRequest.Builder unauthenticatedRequest(String path) {
        return HttpRequest.newBuilder(baseUri.resolve(path))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
    }

    /**
     * Wandelt ein Objekt in einen JSON-Anfragerumpf um.
     *
     * @param body das umzuwandelnde Objekt (darf {@code null} sein)
     * @return der Anfragerumpf
     * @throws ApiClientException wenn die Umwandlung fehlschlägt
     */
    private HttpRequest.BodyPublisher bodyPublisher(Object body) {
        try {
            return HttpRequest.BodyPublishers.ofString(body == null ? "" : objectMapper.writeValueAsString(body));
        } catch (IOException exception) {
            throw new ApiClientException("Failed to serialize request", exception);
        }
    }

    /**
     * Sendet eine Anfrage und wandelt die Antwort in den angegebenen Typ um.
     *
     * @param requestFactory erzeugt die zu sendende Anfrage
     * @param responseType der erwartete Antworttyp
     * @param <T>          der Antworttyp
     * @return die umgewandelte Antwort oder {@code null} bei leerer Antwort
     * @throws ApiClientException bei einem Fehler der Kommunikation
     */
    private <T> T send(Supplier<HttpRequest> requestFactory, Class<T> responseType) {
        HttpResponse<String> response = sendWithAuthRetry(requestFactory);
        ensureSuccess(response);
        if (responseType == Void.class || response.body().isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(response.body(), responseType);
        } catch (IOException exception) {
            throw new ApiClientException("Server request failed", exception);
        }
    }

    /**
     * Wie {@link #send(Supplier, Class)}, jedoch für generische Typen.
     *
     * @param requestFactory erzeugt die zu sendende Anfrage
     * @param responseType die Typreferenz der erwarteten Antwort
     * @param <T>          der Antworttyp
     * @return die umgewandelte Antwort
     * @throws ApiClientException bei einem Fehler der Kommunikation
     */
    private <T> T send(Supplier<HttpRequest> requestFactory, TypeReference<T> responseType) {
        HttpResponse<String> response = sendWithAuthRetry(requestFactory);
        ensureSuccess(response);
        try {
            return objectMapper.readValue(response.body(), responseType);
        } catch (IOException exception) {
            throw new ApiClientException("Server request failed", exception);
        }
    }

    /**
     * Sendet die Anfrage und erneuert bei einer 401-Antwort einmalig das
     * Access-Token, bevor die Anfrage wiederholt wird. So bleibt der Benutzer
     * angemeldet, auch wenn das kurzlebige Access-Token abgelaufen ist.
     *
     * @param requestFactory erzeugt die Anfrage (mit aktuellem Access-Token)
     * @return die HTTP-Antwort des Servers
     */
    private HttpResponse<String> sendWithAuthRetry(Supplier<HttpRequest> requestFactory) {
        String triedToken = accessToken;
        HttpResponse<String> response = exchange(requestFactory.get());
        if (response.statusCode() == 401 && reauthorize(triedToken)) {
            response = exchange(requestFactory.get());
        }
        return response;
    }

    /**
     * Sorgt für ein gültiges Access-Token, wenn die letzte Anfrage mit 401
     * abgelehnt wurde. Hat zwischenzeitlich bereits ein anderer Aufruf das
     * Token erneuert, wird kein weiterer Erneuerungsversuch unternommen.
     *
     * @param triedToken das Access-Token, mit dem die fehlgeschlagene Anfrage
     *                   gesendet wurde
     * @return {@code true}, wenn nun ein (potenziell neues) Token vorliegt und
     *         ein erneuter Versuch sinnvoll ist
     */
    private synchronized boolean reauthorize(String triedToken) {
        if (accessToken != null && !accessToken.equals(triedToken)) {
            return true;
        }
        return refreshAccessToken();
    }

    /**
     * Tauscht das Refresh-Token gegen ein neues Access-Token (und ein neues
     * Refresh-Token) ein.
     *
     * @return {@code true}, wenn die Erneuerung erfolgreich war
     */
    private boolean refreshAccessToken() {
        if (refreshToken == null || refreshToken.isBlank()) {
            return false;
        }
        try {
            HttpRequest request = unauthenticatedRequest("/auth/refresh")
                    .POST(bodyPublisher(new RefreshTokenRequest(refreshToken)))
                    .build();
            HttpResponse<String> response = exchange(request);
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return false;
            }
            store(objectMapper.readValue(response.body(), AuthResponse.class));
            return true;
        } catch (ApiClientException | IOException exception) {
            return false;
        }
    }

    /**
     * Sendet die Anfrage und versucht es bei einem Verbindungsfehler erneut.
     * <p>
     * Eine schlafende Render-Instanz weist die erste Verbindung oft ab oder
     * trennt sie während des Hochfahrens; ein zweiter Versuch nach kurzer
     * Wartezeit trifft dann meist den bereits wachen Server.
     *
     * @param request die zu sendende Anfrage
     * @return die HTTP-Antwort des Servers
     * @throws ApiClientException wenn auch der erneute Versuch fehlschlägt
     */
    private HttpResponse<String> exchange(HttpRequest request) {
        IOException lastFailure = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException exception) {
                lastFailure = exception;
                if (attempt < MAX_ATTEMPTS) {
                    sleepBeforeRetry();
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new ApiClientException("Server request interrupted", exception);
            }
        }
        throw new ApiClientException("Server request failed", lastFailure);
    }

    /**
     * Wartet kurz vor einem erneuten Versuch, damit der Server hochfahren kann.
     */
    private void sleepBeforeRetry() {
        try {
            Thread.sleep(RETRY_BACKOFF.toMillis());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiClientException("Server request interrupted", exception);
        }
    }

    /**
     * Prüft, ob die Antwort einen Erfolgsstatuscode hat.
     *
     * @param response die zu prüfende Antwort
     * @throws ApiClientException wenn der Statuscode keinen Erfolg anzeigt
     */
    private void ensureSuccess(HttpResponse<String> response) {
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new ApiClientException("Server returned " + response.statusCode() + ": " + errorMessage(response.body()));
        }
    }

    /**
     * Liest die Fehlermeldung aus dem Antwortrumpf (Feld {@code error}) aus.
     *
     * @param body der Antwortrumpf
     * @return die Fehlermeldung oder der gesamte Rumpf
     */
    private String errorMessage(String body) {
        if (body == null || body.isBlank()) {
            return "No error details returned.";
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode error = root.get("error");
            if (error != null && error.isTextual() && !error.asText().isBlank()) {
                return error.asText();
            }
        } catch (IOException ignored) {
        }
        return body;
    }
}
