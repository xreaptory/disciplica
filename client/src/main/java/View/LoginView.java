package View;

import View.api.ApiClient;
import View.api.ApiClientException;
import View.api.SessionStore;
import com.disciplica.shared.auth.AuthResponse;
import com.disciplica.shared.auth.LoginRequest;
import com.disciplica.shared.auth.RegisterRequest;
import com.sun.net.httpserver.HttpServer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.ConnectException;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class LoginView {
    private static final String GOOGLE_LOGO_URL = "https://developers.google.com/identity/images/g-logo.png";
    private static final Duration OAUTH_TIMEOUT = Duration.ofMinutes(3);
    // Render free tier can take 50–90s to wake from cold — use a generous timeout.
    private static final Duration SERVER_WARMUP_TIMEOUT = Duration.ofSeconds(90);
    private static final String DEFAULT_HOSTED_API_BASE_URL = "https://disciplica-api-now5.onrender.com";

    private final Stage stage;
    private final SessionStore sessionStore;
    private final Runnable onAuthenticated;
    private final ApiClient apiClient;
    private final String apiBaseUrl;
    private boolean registerMode;

    private final TextField usernameField = new TextField();
    private final TextField emailField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Button submitButton = new Button("Log In");
    private final Button googleButton = new Button("Continue with Google");
    private final Button offlineButton = new Button("Continue Offline");
    private final Hyperlink modeLink = new Hyperlink("Create an account");
    private final Label title = new Label("Welcome to Disciplica");
    private final Label serverStatusLabel = new Label("");

    public LoginView(Stage stage, SessionStore sessionStore, Runnable onAuthenticated) {
        this.stage = stage;
        this.sessionStore = sessionStore;
        this.onAuthenticated = onAuthenticated;
        this.apiBaseUrl = resolveBaseUrl();
        this.apiClient = new ApiClient(apiBaseUrl);
        show();
    }

    private void show() {
        usernameField.setPromptText("Username");
        emailField.setPromptText("Email");
        passwordField.setPromptText("Password");
        usernameField.setVisible(false);
        usernameField.setManaged(false);

        title.getStyleClass().add("auth-title");
        Label subtitle = new Label("Sign in to sync tasks, join parties, and play with friends.");
        subtitle.getStyleClass().add("auth-subtitle");

        serverStatusLabel.getStyleClass().add("auth-server-status");
        serverStatusLabel.setVisible(false);
        serverStatusLabel.setManaged(false);

        for (TextField field : new TextField[]{usernameField, emailField, passwordField}) {
            field.getStyleClass().add("habitica-field");
            field.setPrefWidth(320);
        }

        submitButton.getStyleClass().addAll("habitica-button", "success");
        submitButton.setPrefWidth(320);
        submitButton.setOnAction(event -> submit());

        googleButton.getStyleClass().addAll("habitica-button", "google-button");
        googleButton.setPrefWidth(320);
        googleButton.setGraphic(createGoogleLogo());
        googleButton.setContentDisplay(ContentDisplay.LEFT);
        googleButton.setGraphicTextGap(12);
        googleButton.setOnAction(event -> google());

        offlineButton.getStyleClass().addAll("habitica-button", "offline-button");
        offlineButton.setPrefWidth(320);
        offlineButton.setOnAction(event -> continueOffline());

        modeLink.setOnAction(event -> toggleMode());

        Label offlineHint = new Label("Offline mode stores data only on this computer. Multiplayer and Google login need the hosted server.");
        offlineHint.getStyleClass().add("auth-offline-hint");

        VBox card = new VBox(14, title, subtitle, usernameField, emailField, passwordField,
                submitButton, googleButton, serverStatusLabel, offlineButton, offlineHint, modeLink);
        card.getStyleClass().add("auth-card");
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(28));

        Node logo = createDisciplicaLogo();
        Label brandName = new Label("Disciplica");
        brandName.getStyleClass().add("auth-brand-name");
        Label brandTagline = new Label("Level up your life");
        brandTagline.getStyleClass().add("auth-brand-tagline");

        VBox brand = new VBox(6, logo, brandName, brandTagline);
        brand.getStyleClass().add("auth-brand");
        brand.setAlignment(Pos.CENTER);

        VBox layout = new VBox(20, brand, card);
        layout.setAlignment(Pos.CENTER);
        StackPane root = new StackPane(layout);
        root.getStyleClass().add("auth-root");

        Scene scene = new Scene(root, 760, 680);
        var css = getClass().getResource("/css/habitica-theme.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        stage.setTitle("Disciplica Login");
        stage.setScene(scene);
        stage.show();
    }

    private Node createDisciplicaLogo() {
        // Pentagon shield: flat top, pointed bottom
        Polygon shield = new Polygon(
                0.0, 0.0,
                64.0, 0.0,
                64.0, 44.0,
                32.0, 64.0,
                0.0, 44.0
        );
        shield.getStyleClass().add("logo-shield");

        Label letter = new Label("D");
        letter.getStyleClass().add("logo-letter");
        letter.setTranslateY(-6);

        StackPane logoPane = new StackPane(shield, letter);
        logoPane.setPrefSize(64, 64);
        logoPane.setMaxSize(64, 64);
        return logoPane;
    }

    private void submit() {
        String email = emailField.getText();
        String password = passwordField.getText();
        runAuth(() -> registerMode
                ? apiClient.register(new RegisterRequest(usernameField.getText(), email, password))
                : apiClient.login(new LoginRequest(email, password)));
    }

    private void google() {
        runAuth(this::runGoogleOAuth);
    }

    private void continueOffline() {
        stage.hide();
        onAuthenticated.run();
    }

    private void runAuth(AuthCall authCall) {
        submitButton.setDisable(true);
        googleButton.setDisable(true);
        offlineButton.setDisable(true);
        Task<AuthResponse> task = new Task<>() {
            @Override
            protected AuthResponse call() {
                return authCall.call();
            }
        };
        task.setOnSucceeded(event -> {
            AuthResponse response = task.getValue();
            apiClient.store(response);
            sessionStore.authenticate(apiClient, response.user());
            stage.hide();
            onAuthenticated.run();
        });
        task.setOnFailed(event -> {
            submitButton.setDisable(false);
            googleButton.setDisable(false);
            offlineButton.setDisable(false);
            clearServerStatus();
            Throwable exception = task.getException();
            showError("Sign-in failed", exception.getMessage());
        });
        Thread thread = new Thread(task, "disciplica-auth");
        thread.setDaemon(true);
        thread.start();
    }

    private void toggleMode() {
        registerMode = !registerMode;
        usernameField.setVisible(registerMode);
        usernameField.setManaged(registerMode);
        submitButton.setText(registerMode ? "Create Account" : "Log In");
        modeLink.setText(registerMode ? "Already have an account?" : "Create an account");
        title.setText(registerMode ? "Create your hero" : "Welcome to Disciplica");
        Platform.runLater(usernameField::requestFocus);
    }

    private void setServerStatus(String message) {
        Platform.runLater(() -> {
            serverStatusLabel.setText(message);
            serverStatusLabel.setVisible(true);
            serverStatusLabel.setManaged(true);
        });
    }

    private void clearServerStatus() {
        Platform.runLater(() -> {
            serverStatusLabel.setText("");
            serverStatusLabel.setVisible(false);
            serverStatusLabel.setManaged(false);
        });
    }

    private String resolveBaseUrl() {
        String property = System.getProperty("disciplica.apiBaseUrl");
        if (isUsableApiOverride(property)) {
            return property;
        }
        String env = System.getenv("DISCIPLICA_API_BASE_URL");
        if (isUsableApiOverride(env)) {
            return env;
        }
        try (var stream = LoginView.class.getResourceAsStream("/disciplica-client.properties")) {
            if (stream != null) {
                Properties properties = new Properties();
                properties.load(stream);
                String configuredUrl = properties.getProperty("apiBaseUrl");
                if (configuredUrl != null && !configuredUrl.isBlank()) {
                    return configuredUrl;
                }
            }
        } catch (IOException ignored) {
        }
        return DEFAULT_HOSTED_API_BASE_URL;
    }

    private boolean isUsableApiOverride(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        if (!isLocalApiUrl(value)) {
            return true;
        }
        return Boolean.parseBoolean(System.getProperty("disciplica.allowLocalApi", "false"))
                || Boolean.parseBoolean(System.getenv().getOrDefault("DISCIPLICA_ALLOW_LOCAL_API", "false"));
    }

    private boolean isLocalApiUrl(String value) {
        String normalized = value.toLowerCase();
        return normalized.contains("localhost")
                || normalized.contains("127.0.0.1")
                || normalized.contains("0.0.0.0");
    }

    private AuthResponse runGoogleOAuth() {
        HttpServer callbackServer = null;
        try {
            callbackServer = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
            int port = callbackServer.getAddress().getPort();
            String redirectUri = "http://127.0.0.1:" + port + "/oauth2/callback";
            CompletableFuture<Map<String, String>> callbackFuture = new CompletableFuture<>();

            callbackServer.createContext("/oauth2/callback", exchange -> {
                Map<String, String> params = parseQuery(exchange.getRequestURI().getRawQuery());
                String body = params.containsKey("error")
                        ? "<h2>Google sign-in failed.</h2><p>You can close this tab.</p>"
                        : "<h2>Google sign-in complete.</h2><p>You can close this tab and return to Disciplica.</p>";
                byte[] response = body.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
                exchange.sendResponseHeaders(200, response.length);
                try (OutputStream stream = exchange.getResponseBody()) {
                    stream.write(response);
                }
                callbackFuture.complete(params);
            });
            callbackServer.start();

            ensureBackendReachable();
            openBrowser(apiBaseUrl + "/auth/google/desktop/start?appRedirectUri=" + urlEncode(redirectUri));
            Map<String, String> params = callbackFuture.get(OAUTH_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
            if (params.containsKey("error")) {
                throw new ApiClientException("Google sign-in failed: " + params.get("error"));
            }
            String code = params.get("code");
            if (code == null || code.isBlank()) {
                throw new ApiClientException("Google sign-in did not return a Disciplica login code.");
            }
            return apiClient.googleDesktopComplete(code);
        } catch (ApiClientException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApiClientException("Google sign-in could not complete", exception);
        } finally {
            if (callbackServer != null) {
                callbackServer.stop(0);
            }
        }
    }

    private void ensureBackendReachable() {
        setServerStatus("⏳ Waking up the server — this takes up to 60 s on first launch. Please wait…");
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(apiBaseUrl + "/healthz"))
                    .timeout(SERVER_WARMUP_TIMEOUT)
                    .GET()
                    .build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            clearServerStatus();
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ApiClientException("Disciplica server returned " + response.statusCode()
                        + " while checking " + apiBaseUrl + ".");
            }
        } catch (ConnectException exception) {
            clearServerStatus();
            throw new ApiClientException("""
                    Disciplica server is not running.

                    The client tried:
                    %s

                    If this is a consumer build, the hosted Disciplica backend must be online.
                    For local development, set DISCIPLICA_API_BASE_URL=http://localhost:8080 and DISCIPLICA_ALLOW_LOCAL_API=true.""".formatted(apiBaseUrl), exception);
        } catch (IOException exception) {
            clearServerStatus();
            throw new ApiClientException("Could not reach Disciplica server at " + apiBaseUrl
                    + ". The server may still be starting up — try again in a moment.", exception);
        } catch (InterruptedException exception) {
            clearServerStatus();
            Thread.currentThread().interrupt();
            throw new ApiClientException("Server reachability check was interrupted", exception);
        }
    }

    private void openBrowser(String uri) throws IOException {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            throw new ApiClientException("No desktop browser is available for Google Sign-In.");
        }
        Desktop.getDesktop().browse(URI.create(uri));
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> params = new HashMap<>();
        if (query == null || query.isBlank()) {
            return params;
        }
        for (String pair : query.split("&")) {
            int separator = pair.indexOf('=');
            String key = separator >= 0 ? pair.substring(0, separator) : pair;
            String value = separator >= 0 ? pair.substring(separator + 1) : "";
            params.put(urlDecode(key), urlDecode(value));
        }
        return params;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private ImageView createGoogleLogo() {
        ImageView logo = new ImageView(new Image(GOOGLE_LOGO_URL, 20, 20, true, true, true));
        logo.setFitWidth(20);
        logo.setFitHeight(20);
        logo.setPreserveRatio(true);
        return logo;
    }

    private void showError(String header, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        TextArea textArea = new TextArea(message == null ? "Unknown error." : message);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefColumnCount(54);
        textArea.setPrefRowCount(10);
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefWidth(620);
        alert.showAndWait();
    }

    @FunctionalInterface
    private interface AuthCall {
        AuthResponse call() throws ApiClientException;
    }
}
