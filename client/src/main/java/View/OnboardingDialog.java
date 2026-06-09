package View;

import java.util.ArrayList;
import java.util.List;

import View.api.ApiClientException;
import View.api.SessionStore;
import View.avatar.AvatarPixelRenderer;
import View.avatar.AvatarState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Begrüßungsdialog, in dem neue Benutzer ihren Avatar gestalten und ihre
 * ersten Schwerpunkte auswählen.
 * <p>
 * Die Auswahl (Körper, Haut, Haare, Schwerpunkte) wird sofort in einer
 * Vorschau dargestellt und beim Abschluss im {@link SessionStore} gespeichert.
 */
public class OnboardingDialog {
    private final Stage owner;
    private final SessionStore sessionStore;
    private final Canvas avatar = new Canvas(380, 230);
    private final AvatarState avatarState;
    private final List<String> focusAreas = new ArrayList<>();

    /**
     * Erzeugt den Dialog ohne Sitzungsspeicher (z.&nbsp;B. zur Vorschau).
     *
     * @param owner das Eltern-Fenster
     */
    public OnboardingDialog(Stage owner) {
        this(owner, null);
    }

    /**
     * Erzeugt den Dialog mit Zugriff auf die aktuelle Sitzung.
     *
     * @param owner        das Eltern-Fenster
     * @param sessionStore der Sitzungsspeicher (darf {@code null} sein)
     */
    public OnboardingDialog(Stage owner, SessionStore sessionStore) {
        this.owner = owner;
        this.sessionStore = sessionStore;
        this.avatarState = sessionStore == null ? AvatarState.defaults() : sessionStore.avatarState();
    }

    /**
     * Baut den Dialog auf und zeigt ihn modal an, bis der Benutzer ihn
     * abschließt.
     */
    public void show() {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Create your hero");

        Label guideName = new Label("Justin");
        guideName.getStyleClass().add("onboarding-guide-name");
        Label guide = new Label("Hello there! I am your Disciplica guide. Choose your hero and your first quest focus.");
        guide.getStyleClass().add("onboarding-guide-text");
        guide.setWrapText(true);
        VBox guideBox = new VBox(guideName, guide);
        guideBox.getStyleClass().add("onboarding-guide-box");

        avatar.getStyleClass().add("avatar-stage");
        renderAvatar();

        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("onboarding-tabs");
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().add(new Tab("Body", bodyContent()));
        tabs.getTabs().add(new Tab("Skin", optionGrid("Choose your skin color", "skin",
                "Warm", "Tan", "Brown", "Dark", "Fantasy")));
        tabs.getTabs().add(new Tab("Hair", hairContent()));
        tabs.getTabs().add(new Tab("Focus", focusGrid()));

        Button finish = new Button("Finish");
        finish.getStyleClass().addAll("habitica-button", "success");
        finish.setPrefWidth(160);
        finish.setOnAction(event -> {
            saveAvatar();
            dialog.close();
        });

        HBox actionBar = new HBox(finish);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(18, guideBox, avatar, tabs, actionBar);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(28));
        card.getStyleClass().add("onboarding-card");

        Scene scene = new Scene(card, 780, 760);
        var css = getClass().getResource("/css/habitica-theme.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /**
     * {@return der Inhalt des Reiters „Body“ (Körpergröße und Oberteilfarbe)}
     */
    private VBox bodyContent() {
        VBox content = new VBox(16,
                optionGrid("Choose your body size", "body", "Small", "Medium", "Tall"),
                optionGrid("Choose your shirt color", "shirt", "Blue", "Green", "Pink", "Yellow", "Gray"));
        content.setPadding(new Insets(4));
        return content;
    }

    /**
     * {@return der Inhalt des Reiters „Hair“ (Frisur und Haarfarbe)}
     */
    private VBox hairContent() {
        VBox content = new VBox(16,
                optionGrid("Choose your hair style", "hairStyle", "Short", "Long", "Bangs", "Spikes"),
                optionGrid("Choose your hair color", "hairColor", "Brown", "Black", "Blonde", "Red", "White"));
        content.setPadding(new Insets(4));
        return content;
    }

    /**
     * Baut ein Raster aus Auswahlschaltflächen für eine Avatar-Eigenschaft.
     *
     * @param titleText die Überschrift des Rasters
     * @param category  die Kategorie (z.&nbsp;B. „body“, „skin“)
     * @param options   die möglichen Werte
     * @return das aufgebaute Auswahlraster
     */
    private GridPane optionGrid(String titleText, String category, String... options) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("onboarding-option-grid");
        grid.setHgap(14);
        grid.setVgap(16);
        grid.setPadding(new Insets(20, 14, 20, 14));

        Label title = new Label(titleText);
        title.getStyleClass().add("onboarding-section-title");
        grid.add(title, 0, 0, Math.max(1, options.length), 1);

        List<Button> buttons = new ArrayList<>();
        for (int index = 0; index < options.length; index++) {
            String option = options[index];
            Button button = new Button(option);
            button.getStyleClass().addAll("habitica-button", "avatar-option-button");
            button.setPrefWidth(126);
            button.setMinHeight(44);
            button.setOnAction(event -> {
                setSelection(category, option);
                updateSelectedButtons(buttons, option);
                renderAvatar();
            });
            buttons.add(button);
            grid.add(button, index % 5, 1 + index / 5);
        }
        updateSelectedButtons(buttons, currentSelection(category));
        return grid;
    }

    /**
     * Baut das Raster mit den auswählbaren Schwerpunkten (Mehrfachauswahl).
     *
     * @return das aufgebaute Schwerpunkt-Raster
     */
    private GridPane focusGrid() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("onboarding-option-grid");
        grid.setHgap(28);
        grid.setVgap(16);
        grid.setPadding(new Insets(20, 14, 20, 14));

        Label title = new Label("Choose what you want to work on");
        title.getStyleClass().add("onboarding-section-title");
        grid.add(title, 0, 0, 2, 1);

        String[] labels = {"Work", "Exercise", "Health & Wellness", "School", "Chores", "Creativity", "Self-Care"};
        for (int index = 0; index < labels.length; index++) {
            String label = labels[index];
            CheckBox checkBox = new CheckBox(label);
            checkBox.getStyleClass().add("onboarding-checkbox");
            checkBox.setSelected(focusAreas.contains(label));
            checkBox.selectedProperty().addListener((observable, wasSelected, isSelected) -> {
                if (isSelected && !focusAreas.contains(label)) {
                    focusAreas.add(label);
                } else if (!isSelected) {
                    focusAreas.remove(label);
                }
            });
            grid.add(checkBox, index % 2, 1 + index / 2);
        }
        return grid;
    }

    /**
     * Übernimmt eine Auswahl in den Avatar-Zustand.
     *
     * @param category die Kategorie der Auswahl
     * @param option   der gewählte Wert
     * @throws IllegalArgumentException bei einer unbekannten Kategorie
     */
    private void setSelection(String category, String option) {
        switch (category) {
            case "body" -> avatarState.setBodySize(option);
            case "shirt" -> avatarState.setShirtColor(option);
            case "skin" -> avatarState.setSkinColor(option);
            case "hairStyle" -> {
                avatarState.setHairStyle(option);
                avatarState.setHairBangs("Bangs".equals(option) ? "Bangs" : "None");
            }
            case "hairColor" -> avatarState.setHairColor(option);
            default -> throw new IllegalArgumentException("Unknown avatar option category: " + category);
        }
    }

    /**
     * Gibt die aktuell gewählte Option einer Kategorie zurück.
     *
     * @param category die Kategorie
     * @return der aktuell gewählte Wert
     */
    private String currentSelection(String category) {
        return switch (category) {
            case "body" -> avatarState.getBodySize();
            case "shirt" -> avatarState.getShirtColor();
            case "skin" -> avatarState.getSkinColor();
            case "hairStyle" -> avatarState.getHairStyle();
            case "hairColor" -> avatarState.getHairColor();
            default -> "";
        };
    }

    /**
     * Hebt die zur Auswahl passende Schaltfläche hervor.
     *
     * @param buttons        die Schaltflächen einer Kategorie
     * @param selectedOption die aktuell gewählte Option
     */
    private void updateSelectedButtons(List<Button> buttons, String selectedOption) {
        for (Button button : buttons) {
            button.getStyleClass().remove("selected");
            if (button.getText().equals(selectedOption)) {
                button.getStyleClass().add("selected");
            }
        }
    }

    /**
     * Zeichnet die Avatar-Vorschau neu.
     */
    private void renderAvatar() {
        AvatarPixelRenderer.render(avatar, avatarState);
    }

    /**
     * Speichert den gestalteten Avatar in der Sitzung. Schlägt das Speichern
     * auf dem Server fehl, wird ein Hinweis angezeigt.
     */
    private void saveAvatar() {
        if (sessionStore == null) {
            return;
        }
        try {
            sessionStore.updateAvatar(avatarState);
        } catch (ApiClientException exception) {
            Alert alert = new Alert(Alert.AlertType.WARNING, exception.getMessage());
            alert.setHeaderText("Avatar saved for this session");
            alert.showAndWait();
        }
    }
}
