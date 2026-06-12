package View;

import com.google.inject.Injector;
import javafx.application.Platform;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Interpolator;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import model.Properties;
import model.domain.model.AbstractTask;
import model.domain.model.DailyHabit;
import model.domain.model.WeeklyHabit;
import model.service.UserService;
import View.api.SessionStore;
import View.api.ApiClientException;
import View.avatar.AvatarPixelRenderer;
import View.avatar.AvatarState;
import com.disciplica.shared.party.ChatMessageDto;
import com.disciplica.shared.party.PartyDto;
import com.disciplica.shared.party.PartyInviteDto;

import java.io.IOException;
import java.io.File;
import javax.imageio.ImageIO;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Das Hauptfenster der Anwendung.
 * <p>
 * Baut die gesamte Benutzeroberfläche auf – Navigationsleiste, Dashboard,
 * Gewohnheitsverwaltung, Statistiken, Avatar samt Shop und den Gruppenbereich
 * (Party) – und verbindet sie mit dem {@link MainController}. Die Klasse
 * erweitert {@link Stage} und stellt zahlreiche Hilfsmethoden zum Erzeugen und
 * Gestalten der einzelnen Oberflächenbestandteile bereit.
 */
public class View extends Stage {

    final Button dashboardBTN = new Button();
    final Button habitsBTN = new Button();
    final Button statsBTN = new Button();
    final Button partyBTN = new Button();

    MainController mainController;

    final Button[] buttonsLMenu = {dashboardBTN, habitsBTN, statsBTN, partyBTN};

    final HBox hbox = new HBox();

    public StackPane stackPane = new StackPane();

    public TextField nameTF,descriptionTF,pointsTF;

    public TextField nameField, typeField, pointsField, descriptionField, streakField;

    public CheckBox isCompletedCB;

    public TextField usernameTF, levelTF, titelTF, expirienceTF;
    public ProgressBar xpProgressBar;
    public ProgressBar hpProgressBar;
    public TextField goldTF, healthTF;

    public Button addButton,removeButton,changeButton;

    public ListView<String> listViewTasks;

    public ComboBox<String> comboBox;
    public ComboBox<String> categoryComboBox;
    public Spinner<Integer> durationSpinner;

    final Button completeButton = new Button();
    final Button saveButton = new Button();
    private Timeline dashboardRefreshTimeline;
    private Timeline inviteNotificationTimeline;
    private final Set<UUID> seenInviteIds = ConcurrentHashMap.newKeySet();
    private XYChart.Series<String, Number> dashboardCompletionRateSeries;
    private XYChart.Series<String, Number> dashboardCategoryStrengthSeries;
    private XYChart.Series<Number, String> dashboardStreakSeries;
    private XYChart.Series<String, Number> dashboardXpAreaSeries;
    private ComboBox<String> dashboardRangeSelector;
    private VBox dashboardContainer;

    ObservableList<String> itemsObservable;
    private StackPane loadingOverlay;
    private Label loadingLabel;
    private Canvas avatarPixelCanvas;
    private ComboBox<String> skinToneSelector;
    private ComboBox<String> hairStyleSelector;
    private ComboBox<String> hairColorSelector;
    private ComboBox<String> beardStyleSelector;
    private SessionStore sessionStore;
    private int lastKnownLevel = -1;
    private String equippedWeapon = "None";
    private String equippedArmor = "None";
    private String equippedHeadgear = "None";
    private final ResourceBundle bundle;

    /**
     * {@return die Listenansicht der Aufgaben}
     */
    public ListView<String> getListViewTasks() {
        return listViewTasks;
    }

    /**
     * Standardkonstruktor, der nicht verwendet werden darf – die Klasse
     * benötigt den Abhängigkeits-Container.
     *
     * @throws IllegalStateException immer; bitte den Konstruktor mit
     *                               {@link Injector} verwenden
     */
    public View() {
        throw new IllegalStateException("Use DI-enabled constructor");
    }

    /**
     * Baut das Hauptfenster vollständig auf: lädt die Texte, erzeugt die
     * Navigationsleiste, verbindet die Oberfläche mit dem {@link MainController},
     * zeigt das Dashboard an und lädt im Hintergrund die Daten.
     *
     * @param injector der Abhängigkeits-Container, der die benötigten Dienste
     *                 bereitstellt
     */
    public View(Injector injector) {
        this.bundle = loadBundle();
        dashboardBTN.setText(t("nav.dashboard"));
        habitsBTN.setText(t("nav.habits"));
        statsBTN.setText(t("nav.stats"));
        partyBTN.setText("Party");
        completeButton.setText(t("habits.complete"));
        saveButton.setText(t("habits.save"));

        UserService userService = injector.getInstance(UserService.class);
        sessionStore = injector.getInstance(SessionStore.class);
        mainController = new MainController(this, userService, sessionStore);

        Scene scene = new Scene(hbox, 1180, 720);
        var css = getClass().getResource("/css/habitica-theme.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        hbox.getStyleClass().add("app-shell");
        HBox.setHgrow(stackPane, Priority.ALWAYS);
        stackPane.getStyleClass().add("app-content");
        setTitle(t("app.title"));
        if (Properties.applicationImageIconAsICO != null) {
            getIcons().add(Properties.applicationImageIconAsICO);
        }

        VBox leftMenu = new VBox(14);
        leftMenu.setPadding(new Insets(24, 12, 24, 12));
        leftMenu.setPrefWidth(150);
        leftMenu.setMinWidth(150);
        leftMenu.getStyleClass().add("habitica-nav");

        Label navTitle = new Label("Disciplica");
        navTitle.getStyleClass().add("brand-title");
        navTitle.setMaxWidth(Double.MAX_VALUE);
        navTitle.setAlignment(Pos.CENTER);

        for (Button b : buttonsLMenu) {
            b.setPrefSize(126, 48);
            b.setMaxWidth(Double.MAX_VALUE);
            b.setFocusTraversable(false);
            styleNavButton(b, false);
            b.setOnMouseEntered(e -> animateDropdown(b, true));
            b.setOnMouseExited(e -> animateDropdown(b, false));
            b.addEventHandler(Event.ANY,mainController);
        }
        dashboardBTN.setTooltip(new Tooltip("Dashboard"));
        habitsBTN.setTooltip(new Tooltip("Habits"));
        statsBTN.setTooltip(new Tooltip("Stats"));
        partyBTN.setTooltip(new Tooltip("Party"));
        leftMenu.getChildren().addAll(navTitle, dashboardBTN, habitsBTN, statsBTN, partyBTN);

        hbox.getChildren().add(leftMenu);

        this.setOnCloseRequest(event -> {
            event.consume();
            if (inviteNotificationTimeline != null) {
                inviteNotificationTimeline.stop();
            }
            mainController.saveAllAsync(this::close);
        });

        hbox.getChildren().add(stackPane);
        initializeLoadingOverlay();
        setScene(scene);
        setResizable(true);
        openDashboard();
        centerOnScreen();
        show();
        configureGlobalKeyboardShortcuts(scene);
        applyAccessibility(stackPane);
        mainController.loadDataAsync(this::refreshDashboardData);
        startInviteNotifications();
    }

    /**
     * Startet die regelmäßige Prüfung auf neue Gruppeneinladungen. Sobald eine
     * bisher unbekannte offene Einladung auftaucht, wird der Benutzer per
     * Hinweisfenster benachrichtigt.
     */
    private void startInviteNotifications() {
        pollPartyInvites();
        inviteNotificationTimeline = new Timeline(
                new KeyFrame(Duration.seconds(25), event -> pollPartyInvites()));
        inviteNotificationTimeline.setCycleCount(Timeline.INDEFINITE);
        inviteNotificationTimeline.play();
    }

    /**
     * Fragt im Hintergrund die offenen Einladungen ab und meldet neu
     * hinzugekommene. Der Netzwerkaufruf läuft bewusst außerhalb des
     * JavaFX-Fadens, damit die Oberfläche nicht blockiert.
     */
    private void pollPartyInvites() {
        if (sessionStore == null || !sessionStore.isAuthenticated()) {
            return;
        }
        Thread worker = new Thread(() -> {
            try {
                List<PartyInviteDto> fresh = sessionStore.apiClient().pendingInvites().stream()
                        .filter(invite -> seenInviteIds.add(invite.id()))
                        .toList();
                if (!fresh.isEmpty()) {
                    Platform.runLater(() -> notifyNewInvites(fresh));
                }
            } catch (ApiClientException ignored) {
                // Vorübergehende Server- oder Verbindungsfehler einfach übergehen;
                // der nächste Durchlauf versucht es erneut.
            }
        }, "party-invite-poll");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Zeigt ein Hinweisfenster über neue Gruppeneinladungen an.
     *
     * @param invites die neu hinzugekommenen offenen Einladungen
     */
    private void notifyNewInvites(List<PartyInviteDto> invites) {
        String parties = invites.stream()
                .map(PartyInviteDto::partyName)
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(this);
        alert.setTitle("Party invitation");
        alert.setHeaderText(invites.size() == 1
                ? "You have a new party invitation"
                : "You have " + invites.size() + " new party invitations");
        alert.setContentText("Invited to: " + parties
                + "\nOpen the Party tab to accept or decline.");
        alert.show();
    }

    /**
     * Öffnet die Gewohnheitsverwaltung mit Eingabefeldern und der
     * Aufgabenliste zum Anlegen, Ändern und Löschen von Aufgaben.
     *
     * @throws IOException bei einem Fehler beim Laden der Daten
     */
    public void openHabitMenu() throws IOException {

        GridPane gridPane = new GridPane();
        gridPane.setHgap(14);
        gridPane.setVgap(12);

        nameTF = new TextField();
        nameTF.addEventHandler(Event.ANY,mainController);
        descriptionTF = new TextField();
        descriptionTF.addEventHandler(Event.ANY,mainController);
        pointsTF = new TextField();
        pointsTF.addEventHandler(Event.ANY,mainController);
        streakField = new TextField();
        streakField.addEventHandler(Event.ANY,mainController);


        addButton = new Button("Add Habit");
        addButton.addEventHandler(Event.ANY,mainController);
        configureHabiticaButton(addButton, "success", 128);
        removeButton = new Button("Delete Habit");
        removeButton.addEventHandler(Event.ANY,mainController);
        configureHabiticaButton(removeButton, "danger", 128);
        changeButton = new Button("Update Selected");
        changeButton.addEventHandler(Event.ANY,mainController);
        configureHabiticaButton(changeButton, "info", 144);
        saveButton.setText("Save Data");
        saveButton.addEventHandler(Event.ANY, mainController);
        configureHabiticaButton(saveButton, "secondary", 128);

        itemsObservable = FXCollections.observableArrayList(mainController.getHabits());
        listViewTasks = new ListView<>(itemsObservable);
        listViewTasks.getSelectionModel().selectedItemProperty().addListener(mainController);
        listViewTasks.getSelectionModel().selectFirst();
        listViewTasks.addEventHandler(Event.ANY,mainController);
        listViewTasks.setFixedCellSize(76);
        listViewTasks.setCellFactory(lv -> new ListCell<>() {
            private final HBox row = new HBox();
            private final Region strip = new Region();
            private final VBox content = new VBox();
            private final Label title = new Label();
            private final Label subtitle = new Label();
            {
                strip.setPrefWidth(5);
                strip.setMinWidth(5);
                title.getStyleClass().add("task-title");
                subtitle.getStyleClass().add("task-subtitle");
                title.setWrapText(true);
                subtitle.setWrapText(true);
                content.setSpacing(2);
                content.getChildren().addAll(title, subtitle);
                HBox.setHgrow(content, Priority.ALWAYS);
                row.setSpacing(8);
                row.setAlignment(Pos.CENTER_LEFT);
                row.getChildren().addAll(strip, content);
                row.getStyleClass().add("habitica-card");
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isBlank()) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                String[] parts = item.split(";");
                if (parts.length >= 4) {
                    String type = parts[0];
                    String name = parts[1];
                    String description = parts[2];
                    String points = parts[3];
                    title.setText(name + "  (" + points + " XP)");
                    subtitle.setText(description);
                    if ("D".equals(type)) {
                        strip.getStyleClass().setAll("task-strip-medium");
                    } else if ("W".equals(type)) {
                        strip.getStyleClass().setAll("task-strip-hard");
                    } else {
                        strip.getStyleClass().setAll("task-strip-easy");
                    }
                } else {
                    title.setText(item);
                    subtitle.setText("");
                    strip.getStyleClass().setAll("task-strip-easy");
                }
                setText(null);
                setGraphic(row);
            }
        });
        listViewTasks.setPrefWidth(430);
        listViewTasks.setPrefHeight(520);
        listViewTasks.getStyleClass().add("quest-list");

        comboBox = new ComboBox<>();
        String[] types = {"Daily Habit", "Weekly Habit", "OneTimeTask"};
        comboBox.getItems().addAll(types);
        comboBox.getSelectionModel().selectFirst();
        configureHabiticaDropdown(comboBox, 250);

        categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll("Health", "Work", "Learning", "Fitness", "Chores");
        categoryComboBox.getSelectionModel().select("Work");
        configureHabiticaDropdown(categoryComboBox, 250);

        durationSpinner = new Spinner<>(5, 240, 30, 5);
        durationSpinner.setEditable(true);
        durationSpinner.setPrefHeight(40);
        durationSpinner.setPrefWidth(250);
        durationSpinner.getStyleClass().add("habitica-spinner");
        durationSpinner.getEditor().getStyleClass().add("habitica-field");


        nameTF.setPrefWidth(250);
        configureHabiticaField(nameTF, 250);
        nameTF.setPromptText(t("habits.prompt.name"));
        configureHabiticaField(descriptionTF, 250);
        descriptionTF.setPromptText(t("habits.prompt.description"));
        configureHabiticaField(pointsTF, 250);
        pointsTF.setPromptText(t("habits.prompt.points"));
        pointsTF.setEditable(false);
        configureHabiticaField(streakField, 250);
        FlowPane controlButtons = new FlowPane();
        controlButtons.setHgap(10);
        controlButtons.setVgap(10);
        controlButtons.setPadding(new Insets(8,0,0,0));

        controlButtons.getChildren().addAll(addButton,removeButton,changeButton,saveButton);

        Label l1 = createFieldLabel(t("habits.name"));
        gridPane.add(l1, 0, 0);
        gridPane.add(nameTF, 1, 0);

        Label l2 = createFieldLabel(t("habits.description"));
        gridPane.add(l2, 0, 1);
        gridPane.add(descriptionTF, 1, 1);

        Label l3 = createFieldLabel("XP");
        gridPane.add(l3, 0, 2);
        gridPane.add(pointsTF, 1, 2);

        Label l4 = createFieldLabel("Category");
        gridPane.add(l4, 0, 3);
        gridPane.add(categoryComboBox, 1, 3);

        Label l5 = createFieldLabel("Duration");
        gridPane.add(l5, 0, 4);
        gridPane.add(durationSpinner, 1, 4);

        Label l6 = createFieldLabel(t("habits.type"));
        gridPane.add(l6, 0, 5);
        gridPane.add(comboBox, 1, 5);
        gridPane.add(controlButtons, 0, 6, 2, 1);

        Runnable recomputePoints = () -> {
            int minutes = durationSpinner.getValue() == null ? 30 : durationSpinner.getValue();
            String category = categoryComboBox.getValue() == null ? "Work" : categoryComboBox.getValue();
            String type = comboBox.getValue() == null ? "Daily Habit" : comboBox.getValue();
            int points = MainController.calculateHabitPoints(category, minutes, type);
            pointsTF.setText(String.valueOf(points));
        };
        categoryComboBox.valueProperty().addListener((obs, oldVal, newVal) -> recomputePoints.run());
        comboBox.valueProperty().addListener((obs, oldVal, newVal) -> recomputePoints.run());
        durationSpinner.valueProperty().addListener((obs, oldVal, newVal) -> recomputePoints.run());
        recomputePoints.run();


        nameField = new TextField();
        descriptionField = new TextField();
        pointsField = new TextField();
        typeField = new TextField();
        streakField = new TextField();
        isCompletedCB = new CheckBox();

        VBox formPanel = createPanel(t("habits.title"), gridPane);
        formPanel.setPrefWidth(430);
        HBox contentWrapper = new HBox(22, formPanel, createPanel(t("habits.list"), listViewTasks));
        contentWrapper.setAlignment(Pos.TOP_LEFT);

        VBox page = createPageFrame(t("nav.habits"), t("habits.subtitle"));
        page.getChildren().add(contentWrapper);
        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("page-scroll");
        stackPane.getChildren().setAll(scrollPane);
        setActiveNav(habitsBTN);
        applyAccessibility(stackPane);
    }

    /**
     * Erzeugt eine gestaltete Seitenüberschrift.
     *
     * @param text der Text der Überschrift
     * @return die fertige Beschriftung
     */
    private Label createPageTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("page-title");
        return label;
    }

    /**
     * Erzeugt eine gestaltete Unterüberschrift.
     *
     * @param text der Text der Unterüberschrift
     * @return die fertige Beschriftung
     */
    private Label createPageSubtitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("page-subtitle");
        label.setWrapText(true);
        return label;
    }

    /**
     * Erzeugt eine gestaltete Abschnittsüberschrift.
     *
     * @param text der Text der Überschrift
     * @return die fertige Beschriftung
     */
    private Label createSectionTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-title");
        return label;
    }

    /**
     * Erzeugt eine Beschriftung für ein Eingabefeld.
     *
     * @param text der Text der Beschriftung
     * @return die fertige Beschriftung
     */
    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("field-label");
        return label;
    }

    /**
     * Erzeugt den Grundrahmen einer Seite mit Überschrift und Unterüberschrift.
     *
     * @param title    die Seitenüberschrift
     * @param subtitle die Unterüberschrift
     * @return der Seitenrahmen
     */
    private VBox createPageFrame(String title, String subtitle) {
        VBox page = new VBox(18);
        page.getStyleClass().add("page-frame");
        page.setPadding(new Insets(26));

        VBox header = new VBox(4, createPageTitle(title), createPageSubtitle(subtitle));
        header.getStyleClass().add("page-header");
        page.getChildren().add(header);
        return page;
    }

    /**
     * Erzeugt eine umrahmte Tafel mit Titel und beliebigem Inhalt.
     *
     * @param title   der Titel der Tafel
     * @param content der anzuzeigende Inhalt
     * @return die fertige Tafel
     */
    private VBox createPanel(String title, Node content) {
        VBox panel = new VBox(14, createSectionTitle(title), content);
        panel.getStyleClass().add("habitica-panel");
        return panel;
    }

    /**
     * Versieht ein Textfeld mit dem einheitlichen Stil und einer Breite.
     *
     * @param field das zu gestaltende Textfeld
     * @param width die gewünschte Breite
     */
    private void configureHabiticaField(TextField field, double width) {
        field.getStyleClass().add("habitica-field");
        field.setPrefHeight(40);
        field.setPrefWidth(width);
    }

    /**
     * Versieht eine Schaltfläche mit dem einheitlichen Stil, einer Variante
     * und einer Breite.
     *
     * @param button  die zu gestaltende Schaltfläche
     * @param variant die Stilvariante (z.&nbsp;B. „success“)
     * @param width   die gewünschte Breite
     */
    private void configureHabiticaButton(Button button, String variant, double width) {
        button.getStyleClass().removeAll("habitica-button", "success", "danger", "info", "secondary");
        button.getStyleClass().add("habitica-button");
        if (variant != null && !variant.isBlank()) {
            button.getStyleClass().add(variant);
        }
        button.setPrefHeight(42);
        button.setPrefWidth(width);
        button.setFocusTraversable(false);
        button.setOnMouseEntered(event -> animateDropdown(button, true));
        button.setOnMouseExited(event -> animateDropdown(button, false));
    }

    /**
     * Zeigt eine Ladeanzeige mit einer Meldung über dem Inhalt an.
     *
     * @param message die anzuzeigende Meldung
     */
    public void showLoading(String message) {
        if (loadingLabel != null) {
            loadingLabel.setText(message == null || message.isBlank() ? "Loading..." : message);
        }
        if (loadingOverlay != null && !stackPane.getChildren().contains(loadingOverlay)) {
            loadingOverlay.setMouseTransparent(false);
            stackPane.getChildren().add(loadingOverlay);
        }
    }

    /**
     * Blendet die Ladeanzeige wieder aus.
     */
    public void hideLoading() {
        if (loadingOverlay != null) {
            stackPane.getChildren().remove(loadingOverlay);
        }
    }

    /**
     * Richtet die (zunächst unsichtbare) Ladeanzeige über dem Inhalt ein.
     */
    private void initializeLoadingOverlay() {
        ProgressIndicator indicator = new ProgressIndicator();
        loadingLabel = new Label("Loading...");
        loadingLabel.getStyleClass().add("loading-label");
        VBox box = new VBox(12, indicator, loadingLabel);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.getStyleClass().add("loading-card");

        loadingOverlay = new StackPane(box);
        loadingOverlay.getStyleClass().add("loading-overlay");
        loadingOverlay.setPickOnBounds(true);
        loadingOverlay.prefWidthProperty().bind(stackPane.widthProperty());
        loadingOverlay.prefHeightProperty().bind(stackPane.heightProperty());
    }

    /**
     * Zeigt das Dashboard mit den Diagrammen (Erfüllungsquote, Kategorien,
     * Serien und XP-Verlauf) und dem Zeitraum-Auswahlfeld an.
     */
    public void openDashboard(){
        stackPane.getChildren().clear();
        VBox container = createPageFrame(t("nav.dashboard"), t("dashboard.subtitle"));
        dashboardContainer = container;

        HBox dashboardToolbar = new HBox(12);
        dashboardToolbar.setAlignment(Pos.CENTER_LEFT);
        dashboardToolbar.getStyleClass().add("toolbar-panel");
        Label rangeLabel = createFieldLabel(t("dashboard.range"));
        dashboardRangeSelector = new ComboBox<>();
        dashboardRangeSelector.getItems().setAll(t("dashboard.range.7"), t("dashboard.range.30"), t("dashboard.range.year"));
        dashboardRangeSelector.getSelectionModel().selectFirst();
        configureHabiticaDropdown(dashboardRangeSelector, 170);
        dashboardRangeSelector.valueProperty().addListener((obs, oldValue, newValue) -> refreshDashboardData());
        Button exportButton = new Button(t("dashboard.export"));
        configureHabiticaButton(exportButton, "secondary", 142);
        exportButton.setOnAction(event -> exportDashboardAsImage());
        dashboardToolbar.getChildren().addAll(rangeLabel, dashboardRangeSelector, exportButton);

        CategoryAxis weeklyXAxis = new CategoryAxis();
        NumberAxis weeklyYAxis = new NumberAxis();
        weeklyXAxis.setLabel("Date");
        weeklyYAxis.setLabel("Completion Rate %");
        LineChart<String, Number> weeklyCompletionChart = new LineChart<>(weeklyXAxis, weeklyYAxis);
        weeklyCompletionChart.setTitle(t("dashboard.weeklyCompletion"));
        weeklyCompletionChart.setLegendVisible(false);
        weeklyCompletionChart.setAnimated(false);
        weeklyCompletionChart.setCreateSymbols(false);
        weeklyCompletionChart.setId("dashboardWeeklyCompletionChart");
        dashboardCompletionRateSeries = new XYChart.Series<>();
        weeklyCompletionChart.getData().add(dashboardCompletionRateSeries);

        CategoryAxis categoryXAxis = new CategoryAxis();
        NumberAxis categoryYAxis = new NumberAxis();
        categoryXAxis.setLabel("Category");
        categoryYAxis.setLabel("Strength");
        BarChart<String, Number> categoryStrengthChart = new BarChart<>(categoryXAxis, categoryYAxis);
        categoryStrengthChart.setTitle(t("dashboard.categoryStrength"));
        categoryStrengthChart.setLegendVisible(false);
        categoryStrengthChart.setAnimated(false);
        categoryStrengthChart.setId("dashboardCategoryStrengthChart");
        dashboardCategoryStrengthSeries = new XYChart.Series<>();
        categoryStrengthChart.getData().add(dashboardCategoryStrengthSeries);

        NumberAxis streakXAxis = new NumberAxis();
        CategoryAxis streakYAxis = new CategoryAxis();
        streakXAxis.setLabel("Streak");
        streakYAxis.setLabel("Habit");
        BarChart<Number, String> streakChart = new BarChart<>(streakXAxis, streakYAxis);
        streakChart.setTitle(t("dashboard.streaks"));
        streakChart.setLegendVisible(false);
        streakChart.setAnimated(false);
        streakChart.setId("dashboardStreakChart");
        dashboardStreakSeries = new XYChart.Series<>();
        streakChart.getData().add(dashboardStreakSeries);

        CategoryAxis xpXAxis = new CategoryAxis();
        NumberAxis xpYAxis = new NumberAxis();
        xpXAxis.setLabel("Date");
        xpYAxis.setLabel("XP");
        AreaChart<String, Number> xpHistoryChart = new AreaChart<>(xpXAxis, xpYAxis);
        xpHistoryChart.setTitle(t("dashboard.xpHistory"));
        xpHistoryChart.setLegendVisible(false);
        xpHistoryChart.setAnimated(false);
        xpHistoryChart.setId("dashboardXpHistoryChart");
        dashboardXpAreaSeries = new XYChart.Series<>();
        xpHistoryChart.getData().add(dashboardXpAreaSeries);

        GridPane chartGrid = new GridPane();
        chartGrid.setHgap(16);
        chartGrid.setVgap(16);
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(50);
        chartGrid.getColumnConstraints().addAll(col, col);
        RowConstraints row = new RowConstraints();
        row.setVgrow(Priority.ALWAYS);
        chartGrid.getRowConstraints().addAll(row, row);

        styleHabiticaChart(weeklyCompletionChart);
        styleHabiticaChart(categoryStrengthChart);
        styleHabiticaChart(streakChart);
        styleHabiticaChart(xpHistoryChart);

        weeklyCompletionChart.setPrefHeight(280);
        categoryStrengthChart.setPrefHeight(280);
        streakChart.setPrefHeight(280);
        xpHistoryChart.setPrefHeight(280);

        chartGrid.add(weeklyCompletionChart, 0, 0);
        chartGrid.add(categoryStrengthChart, 1, 0);
        chartGrid.add(streakChart, 0, 1);
        chartGrid.add(xpHistoryChart, 1, 1);

        chartGrid.getStyleClass().add("chart-grid");
        VBox.setVgrow(chartGrid, Priority.ALWAYS);
        container.getChildren().addAll(dashboardToolbar, chartGrid);
        container.setAlignment(Pos.TOP_LEFT);
        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("page-scroll");
        stackPane.getChildren().add(scrollPane);

        refreshDashboardData();
        startLiveDashboardRefresh();
        setActiveNav(dashboardBTN);
        applyAccessibility(stackPane);
    }

    /**
     * Zeigt die Statistikseite mit den Benutzerwerten (Level, Erfahrung,
     * Gold, Lebenspunkte) an.
     */
    public void openStats(){
        stackPane.getChildren().clear();
        HBox statsLayout = new HBox(22);
        statsLayout.setAlignment(Pos.TOP_LEFT);
        GridPane gridPane = new GridPane();

        usernameTF = new TextField();
        usernameTF.setEditable(false);
        configureHabiticaField(usernameTF, 250);
        levelTF = new TextField();
        levelTF.setEditable(false);
        configureHabiticaField(levelTF, 250);
        titelTF = new TextField();
        titelTF.setEditable(false);
        configureHabiticaField(titelTF, 250);
        expirienceTF = new TextField();
        expirienceTF.setEditable(false);
        configureHabiticaField(expirienceTF, 250);
        goldTF = new TextField();
        goldTF.setEditable(false);
        configureHabiticaField(goldTF, 250);
        healthTF = new TextField();
        healthTF.setEditable(false);
        configureHabiticaField(healthTF, 250);
        xpProgressBar = new ProgressBar(0);
        xpProgressBar.setId("xpProgressBar");
        xpProgressBar.setPrefWidth(250);
        xpProgressBar.getStyleClass().addAll("habitica-progress", "xp");
        hpProgressBar = new ProgressBar(0);
        hpProgressBar.setPrefWidth(250);
        hpProgressBar.getStyleClass().addAll("habitica-progress", "hp");

        refreshStatsData();

        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.add(createFieldLabel(t("stats.username")), 0, 1);
        gridPane.add(usernameTF, 1, 1);
        gridPane.add(createFieldLabel(t("stats.level")), 0, 2);
        gridPane.add(levelTF, 1, 2);
        gridPane.add(createFieldLabel(t("stats.title")), 0, 3);
        gridPane.add(titelTF, 1, 3);
        gridPane.add(createFieldLabel(t("stats.experience")), 0, 4);
        gridPane.add(expirienceTF, 1, 4);
        gridPane.add(createFieldLabel(t("stats.gold")), 0, 5);
        gridPane.add(goldTF, 1, 5);
        gridPane.add(createFieldLabel(t("stats.health")), 0, 6);
        gridPane.add(healthTF, 1, 6);
        gridPane.add(createFieldLabel(t("stats.xpProgress")), 0, 7);
        gridPane.add(xpProgressBar, 1, 7);
        gridPane.add(createFieldLabel(t("stats.hpProgress")), 0, 8);
        gridPane.add(hpProgressBar, 1, 8);

        VBox playerPanel = createPanel(t("stats.player"), gridPane);
        playerPanel.setPrefWidth(420);

        VBox avatarAndShop = new VBox(18, createAvatarPanel(), createShopPanel());
        avatarAndShop.setPrefWidth(520);
        statsLayout.getChildren().addAll(playerPanel, avatarAndShop);
        VBox page = createPageFrame(t("nav.stats"), t("stats.subtitle"));
        page.getChildren().add(statsLayout);
        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("page-scroll");
        stackPane.getChildren().add(scrollPane);
        setActiveNav(statsBTN);
        applyAccessibility(stackPane);
    }

    /**
     * Zeigt den Gruppenbereich (Party) mit Mitgliederliste, Chat und
     * Einladungsmöglichkeit an.
     */
    public void openParty() {
        stackPane.getChildren().clear();
        VBox page = createPageFrame("Party", "Invite friends, coordinate quests, and chat together");

        VBox partyDetails = new VBox(12);
        partyDetails.getStyleClass().add("habitica-panel");
        Label statusLabel = createSectionTitle("Your Party");
        TextField partyName = new TextField("Adventurers");
        configureHabiticaField(partyName, 260);
        Button createParty = new Button("Create Party");
        configureHabiticaButton(createParty, "secondary", 150);
        Label memberList = createPageSubtitle("No party loaded yet.");

        HBox createRow = new HBox(10, partyName, createParty);
        createRow.setAlignment(Pos.CENTER_LEFT);
        partyDetails.getChildren().addAll(statusLabel, createRow, memberList);

        VBox invitePanel = new VBox(12);
        invitePanel.getStyleClass().add("habitica-panel");
        TextField inviteField = new TextField();
        inviteField.setPromptText("Friend username or email");
        configureHabiticaField(inviteField, 260);
        Button inviteButton = new Button("Invite to Party");
        configureHabiticaButton(inviteButton, "success", 160);
        Label inviteStatus = createPageSubtitle("");
        invitePanel.getChildren().addAll(createSectionTitle("Invite"),
                new HBox(10, inviteField, inviteButton), inviteStatus);

        VBox invitationsPanel = new VBox(12);
        invitationsPanel.getStyleClass().add("habitica-panel");
        VBox invitationsBox = new VBox(8);
        invitationsPanel.getChildren().addAll(createSectionTitle("Invitations"), invitationsBox);

        VBox chatPanel = new VBox(12);
        chatPanel.getStyleClass().add("habitica-panel");
        ListView<String> chatList = new ListView<>();
        chatList.setPrefHeight(260);
        chatList.getStyleClass().add("quest-list");
        TextField chatField = new TextField();
        chatField.setPromptText("Message your party");
        configureHabiticaField(chatField, 420);
        Button sendButton = new Button("Send");
        configureHabiticaButton(sendButton, "info", 100);
        chatPanel.getChildren().addAll(createSectionTitle("Party Chat"), chatList, new HBox(10, chatField, sendButton));

        // Wird über ein Array gehalten, damit die Annehmen/Ablehnen-Schaltflächen
        // (die innerhalb des Aktualisierens erzeugt werden) erneut aktualisieren können.
        final Runnable[] refreshHolder = new Runnable[1];
        Runnable refreshParty = () -> {
            try {
                PartyDto party = sessionStore.apiClient().currentParty();
                memberList.setText(party.name() + " - " + party.members().size() + " member(s): "
                        + party.members().stream().map(member -> member.username()).reduce((a, b) -> a + ", " + b).orElse(""));
                chatList.getItems().setAll(sessionStore.apiClient().partyMessages().stream()
                        .map(this::formatChatMessage)
                        .toList());
            } catch (ApiClientException exception) {
                memberList.setText("Create a party to start inviting friends.");
                chatList.getItems().clear();
            }
            refreshInvitations(invitationsBox, refreshHolder);
        };
        refreshHolder[0] = refreshParty;

        createParty.setOnAction(event -> {
            try {
                sessionStore.apiClient().createParty(partyName.getText());
            } catch (ApiClientException exception) {
                memberList.setText(exception.getMessage());
            }
            refreshParty.run();
        });
        inviteButton.setOnAction(event -> {
            String target = inviteField.getText();
            try {
                sessionStore.apiClient().invite(target);
                inviteStatus.setText("Invitation sent to " + target + ".");
                inviteField.clear();
            } catch (ApiClientException exception) {
                inviteStatus.setText(exception.getMessage());
            }
            refreshParty.run();
        });
        sendButton.setOnAction(event -> {
            if (!chatField.getText().isBlank()) {
                try {
                    sessionStore.apiClient().sendPartyMessage(chatField.getText());
                    chatField.clear();
                } catch (ApiClientException exception) {
                    inviteStatus.setText(exception.getMessage());
                }
                refreshParty.run();
            }
        });

        refreshParty.run();
        page.getChildren().addAll(partyDetails, invitationsPanel, invitePanel, chatPanel);
        ScrollPane scrollPane = new ScrollPane(page);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("page-scroll");
        stackPane.getChildren().add(scrollPane);
        setActiveNav(partyBTN);
        applyAccessibility(stackPane);
    }

    /**
     * Lädt die offenen Einladungen des Benutzers und stellt sie mit
     * Annehmen-/Ablehnen-Schaltflächen dar.
     *
     * @param container     der Bereich, in dem die Einladungen angezeigt werden
     * @param refreshHolder Halter der Aktualisierungsaktion der Gruppenseite
     */
    private void refreshInvitations(VBox container, Runnable[] refreshHolder) {
        try {
            List<PartyInviteDto> invites = sessionStore.apiClient().pendingInvites();
            // Bereits angezeigte Einladungen merken, damit der Hintergrund-Poll
            // dafür keine erneute Benachrichtigung auslöst.
            invites.forEach(invite -> seenInviteIds.add(invite.id()));
            container.getChildren().clear();
            if (invites.isEmpty()) {
                container.getChildren().add(createPageSubtitle("No pending invitations."));
                return;
            }
            for (PartyInviteDto invite : invites) {
                Label label = createPageSubtitle("Invitation to join \"" + invite.partyName() + "\"");
                Button accept = new Button("Accept");
                configureHabiticaButton(accept, "success", 110);
                Button decline = new Button("Decline");
                configureHabiticaButton(decline, "secondary", 110);
                accept.setOnAction(event -> {
                    try {
                        sessionStore.apiClient().acceptInvite(invite.id());
                    } catch (ApiClientException ignored) {
                        // Aktualisierung unten zeigt den neuen Stand ohnehin an.
                    }
                    refreshHolder[0].run();
                });
                decline.setOnAction(event -> {
                    try {
                        sessionStore.apiClient().declineInvite(invite.id());
                    } catch (ApiClientException ignored) {
                        // Aktualisierung unten zeigt den neuen Stand ohnehin an.
                    }
                    refreshHolder[0].run();
                });
                HBox row = new HBox(10, label, accept, decline);
                row.setAlignment(Pos.CENTER_LEFT);
                container.getChildren().add(row);
            }
        } catch (ApiClientException exception) {
            container.getChildren().clear();
            container.getChildren().add(createPageSubtitle("Could not load invitations."));
        }
    }

    /**
     * Formatiert eine Chat-Nachricht für die Anzeige (Absender und Text).
     *
     * @param message die Chat-Nachricht
     * @return die formatierte Zeile
     */
    private String formatChatMessage(ChatMessageDto message) {
        return message.senderUsername() + ": " + message.message();
    }

    /**
     * Gestaltet eine Navigationsschaltfläche je nach aktivem Zustand.
     *
     * @param button die Schaltfläche
     * @param active {@code true}, wenn die zugehörige Seite gerade aktiv ist
     */
    private void styleNavButton(Button button, boolean active) {
        button.getStyleClass().removeAll("habitica-nav-button", "active");
        button.getStyleClass().add("habitica-nav-button");
        if (active) {
            button.getStyleClass().add("active");
        }
    }

    /**
     * Hebt die angegebene Navigationsschaltfläche als aktiv hervor und setzt
     * die übrigen zurück.
     *
     * @param activeButton die aktive Schaltfläche
     */
    private void setActiveNav(Button activeButton) {
        for (Button button : buttonsLMenu) {
            styleNavButton(button, button == activeButton);
        }
    }

    /**
     * Aktualisiert die auf der Statistikseite angezeigten Benutzerwerte.
     */
    public void refreshStatsData() {
        if (usernameTF == null || levelTF == null || titelTF == null || expirienceTF == null) {
            return;
        }
        MainController.UserStatsSnapshot stats = mainController.getCachedUserStats();
        usernameTF.setText(stats.username());
        int currentLevel = stats.level();
        levelTF.setText(String.valueOf(currentLevel));
        titelTF.setText(stats.title());
        expirienceTF.setText(String.valueOf(stats.experience()));
        goldTF.setText(String.valueOf(stats.gold()));
        healthTF.setText(String.valueOf(stats.health()));
        int level = currentLevel;
        int xp = stats.experience();
        double required = Math.max(1, level * 50.0);
        xpProgressBar.setProgress(Math.min(1.0, xp / required));
        hpProgressBar.setProgress(Math.min(1.0, stats.health() / 50.0));
        updateAvatarLayers();

        if (lastKnownLevel >= 0 && currentLevel > lastKnownLevel) {
            playLevelUpEffect();
        }
        lastKnownLevel = currentLevel;
    }

    /**
     * Erzeugt die Tafel zur Avatar-Gestaltung samt Vorschau und
     * Auswahlfeldern.
     *
     * @return die Avatar-Tafel
     */
    private VBox createAvatarPanel() {
        Label title = createSectionTitle(t("stats.avatar"));

        StackPane avatarCanvas = new StackPane();
        avatarCanvas.setPrefSize(240, 240);
        avatarCanvas.getStyleClass().add("avatar-stage");
        avatarPixelCanvas = new Canvas(340, 220);
        avatarCanvas.getChildren().add(avatarPixelCanvas);
        renderAvatarPixelArt();

        GridPane customizer = new GridPane();
        customizer.setHgap(10);
        customizer.setVgap(8);

        AvatarState avatarState = sessionStore.avatarState();

        skinToneSelector = createAvatarCombo("Warm", "Tan", "Brown", "Dark", "Fantasy");
        skinToneSelector.setValue(avatarState.getSkinColor());
        skinToneSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                avatarState.setSkinColor(newVal);
                persistAvatarProfile();
                renderAvatarPixelArt();
            }
        });

        hairStyleSelector = createAvatarCombo("Short", "Long", "Bangs", "Spikes");
        hairStyleSelector.setValue(avatarState.getHairStyle());
        hairStyleSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                avatarState.setHairStyle(newVal);
                avatarState.setHairBangs("Bangs".equals(newVal) ? "Bangs" : "None");
                persistAvatarProfile();
                renderAvatarPixelArt();
            }
        });

        hairColorSelector = createAvatarCombo("Brown", "Black", "Blonde", "Red", "White");
        hairColorSelector.setValue(avatarState.getHairColor());
        hairColorSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                avatarState.setHairColor(newVal);
                persistAvatarProfile();
                renderAvatarPixelArt();
            }
        });

        beardStyleSelector = createAvatarCombo("None", "Glasses", "Wheelchair");
        beardStyleSelector.setValue(avatarState.getExtra());
        beardStyleSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                avatarState.setExtra(newVal);
                persistAvatarProfile();
                renderAvatarPixelArt();
            }
        });

        customizer.add(createFieldLabel("Skin"), 0, 0);
        customizer.add(skinToneSelector, 1, 0);
        customizer.add(createFieldLabel("Hair"), 0, 1);
        customizer.add(hairStyleSelector, 1, 1);
        customizer.add(createFieldLabel("Hair Color"), 2, 0);
        customizer.add(hairColorSelector, 3, 0);
        customizer.add(createFieldLabel("Extra"), 2, 1);
        customizer.add(beardStyleSelector, 3, 1);

        VBox panel = new VBox(10, title, avatarCanvas, customizer);
        panel.getStyleClass().add("habitica-panel");
        return panel;
    }

    /**
     * Erzeugt ein Auswahlfeld für eine Avatar-Eigenschaft.
     *
     * @param values die auswählbaren Werte
     * @return das gefüllte Auswahlfeld
     */
    private ComboBox<String> createAvatarCombo(String... values) {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(values);
        configureHabiticaDropdown(combo, 120);
        return combo;
    }

    /**
     * Versieht ein Auswahlfeld mit dem einheitlichen Stil, Animationen und
     * einer Breite.
     *
     * @param combo das zu gestaltende Auswahlfeld
     * @param width die gewünschte Breite
     */
    private void configureHabiticaDropdown(ComboBox<String> combo, double width) {
        if (!combo.getStyleClass().contains("habitica-combo")) {
            combo.getStyleClass().add("habitica-combo");
        }
        combo.setPrefWidth(width);
        combo.setMinWidth(width);
        combo.setPrefHeight(40);
        combo.setVisibleRowCount(7);
        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                if (!getStyleClass().contains("habitica-combo-button-cell")) {
                    getStyleClass().add("habitica-combo-button-cell");
                }
            }
        });
        combo.setCellFactory(lv -> new ListCell<>() {
            {
                getStyleClass().add("habitica-combo-popup-cell");
                setOnMouseEntered(event -> animateDropdownCell(this, 5));
                setOnMouseExited(event -> animateDropdownCell(this, 0));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
            }
        });
        combo.setOnMouseEntered(event -> animateDropdown(combo, true));
        combo.setOnMouseExited(event -> animateDropdown(combo, combo.isShowing()));
        combo.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            animateDropdown(combo, isShowing || combo.isHover());
            if (isShowing) {
                playDropdownOpenCue(combo);
            }
        });
    }

    /**
     * Animiert ein Bedienelement leicht nach oben oder zurück (Hover-Effekt).
     *
     * @param node   das zu animierende Element
     * @param raised {@code true}, um das Element anzuheben
     */
    private void animateDropdown(Node node, boolean raised) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(140), node);
        scale.setInterpolator(Interpolator.EASE_BOTH);
        scale.setToX(raised ? 1.015 : 1.0);
        scale.setToY(raised ? 1.015 : 1.0);

        TranslateTransition lift = new TranslateTransition(Duration.millis(140), node);
        lift.setInterpolator(Interpolator.EASE_BOTH);
        lift.setToY(raised ? -1.5 : 0);

        new ParallelTransition(scale, lift).play();
    }

    /**
     * Spielt eine kurze Animation beim Öffnen eines Auswahlfeldes ab.
     *
     * @param node das betroffene Element
     */
    private void playDropdownOpenCue(Node node) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(110), node);
        pulse.setInterpolator(Interpolator.EASE_OUT);
        pulse.setFromX(0.985);
        pulse.setFromY(0.985);
        pulse.setToX(1.025);
        pulse.setToY(1.025);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(2);
        pulse.play();
    }

    /**
     * Animiert einen einzelnen Eintrag eines Auswahlfeldes beim Einblenden.
     *
     * @param node   der Eintrag
     * @param offset der anfängliche Versatz
     */
    private void animateDropdownCell(Node node, double offset) {
        TranslateTransition slide = new TranslateTransition(Duration.millis(120), node);
        slide.setInterpolator(Interpolator.EASE_BOTH);
        slide.setToX(offset);
        slide.play();
    }

    /**
     * Erzeugt die Tafel des Ausrüstungs-Shops.
     *
     * @return die Shop-Tafel
     */
    private VBox createShopPanel() {
        Label title = createSectionTitle(t("stats.shop"));

        GridPane shopGrid = new GridPane();
        shopGrid.setHgap(10);
        shopGrid.setVgap(8);

        Button buySword = createShopButton(t("shop.sword"));
        buySword.setOnAction(e -> purchaseEquipment("weapon", "Sword", 15));
        Button buyAxe = createShopButton(t("shop.axe"));
        buyAxe.setOnAction(e -> purchaseEquipment("weapon", "Axe", 22));
        Button buyGreatsword = createShopButton("Buy Greatsword (120g)");
        buyGreatsword.setOnAction(e -> purchaseEquipment("weapon", "Greatsword", 120));
        Button buyHalberd = createShopButton("Buy Halberd (180g)");
        buyHalberd.setOnAction(e -> purchaseEquipment("weapon", "Halberd", 180));
        Button buyArmor = createShopButton(t("shop.armor"));
        buyArmor.setOnAction(e -> purchaseEquipment("armor", "Armor", 20));
        Button buyCape = createShopButton(t("shop.cape"));
        buyCape.setOnAction(e -> purchaseEquipment("armor", "Cape", 14));
        Button buyDragonArmor = createShopButton("Buy Dragon Armor (250g)");
        buyDragonArmor.setOnAction(e -> purchaseEquipment("armor", "DragonArmor", 250));
        Button buyHelm = createShopButton(t("shop.helm"));
        buyHelm.setOnAction(e -> purchaseEquipment("headgear", "Helm", 18));
        Button buyCrown = createShopButton(t("shop.crown"));
        buyCrown.setOnAction(e -> purchaseEquipment("headgear", "Crown", 25));
        Button buyRoyalCrown = createShopButton("Buy Royal Crown (300g)");
        buyRoyalCrown.setOnAction(e -> purchaseEquipment("headgear", "RoyalCrown", 300));

        shopGrid.addRow(0, buySword, buyAxe);
        shopGrid.addRow(1, buyArmor, buyCape);
        shopGrid.addRow(2, buyHelm, buyCrown);
        shopGrid.addRow(3, buyGreatsword, buyHalberd);
        shopGrid.addRow(4, buyDragonArmor, buyRoyalCrown);

        VBox panel = new VBox(10, title, shopGrid);
        panel.getStyleClass().add("habitica-panel");
        return panel;
    }

    /**
     * Erzeugt eine Kaufschaltfläche für den Shop.
     *
     * @param text die Beschriftung der Schaltfläche
     * @return die fertige Schaltfläche
     */
    private Button createShopButton(String text) {
        Button button = new Button(text);
        configureHabiticaButton(button, "secondary", 190);
        return button;
    }

    /**
     * Kauft einen Ausrüstungsgegenstand, zieht das Gold ab und legt den
     * Gegenstand an.
     *
     * @param slot     der Ausrüstungsplatz (z.&nbsp;B. Waffe, Rüstung)
     * @param item     der zu kaufende Gegenstand
     * @param goldCost die Kosten in Gold
     */
    private void purchaseEquipment(String slot, String item, int goldCost) {
        try {
            mainController.spendGoldAndPersist(goldCost);
            if ("weapon".equals(slot)) {
                equippedWeapon = item;
            } else if ("armor".equals(slot)) {
                equippedArmor = item;
            } else if ("headgear".equals(slot)) {
                equippedHeadgear = item;
            }
            updateAvatarLayers();
            refreshStatsData();
            Alert alert = new Alert(Alert.AlertType.INFORMATION, item + " purchased for " + goldCost + " gold.");
            alert.setHeaderText("Purchase successful");
            alert.showAndWait();
        } catch (IllegalArgumentException exception) {
            Alert alert = new Alert(Alert.AlertType.WARNING, exception.getMessage());
            alert.setHeaderText(t("stats.shop.failed"));
            alert.showAndWait();
        }
    }

    /**
     * Aktualisiert die Darstellung des Avatars nach einer Änderung von
     * Aussehen oder Ausrüstung.
     */
    private void updateAvatarLayers() {
        if (avatarPixelCanvas == null) {
            return;
        }
        renderAvatarPixelArt();
    }

    /**
     * Speichert den aktuellen Avatar-Zustand (über die Sitzung bzw. lokal).
     */
    private void persistAvatarProfile() {
        try {
            sessionStore.updateAvatar(sessionStore.avatarState());
        } catch (ApiClientException exception) {
            Alert alert = new Alert(Alert.AlertType.WARNING, exception.getMessage());
            alert.setHeaderText("Avatar updated for this session");
            alert.show();
        }
    }

    /**
     * Zeichnet die Avatar-Vorschau im Pixel-Stil neu.
     */
    private void renderAvatarPixelArt() {
        if (avatarPixelCanvas == null) {
            return;
        }
        AvatarPixelRenderer.render(avatarPixelCanvas, sessionStore.avatarState(),
                new AvatarPixelRenderer.Equipment(equippedWeapon, equippedArmor, equippedHeadgear));
    }

    /**
     * Spielt eine Animation ab, wenn der Benutzer ein neues Level erreicht.
     */
    private void playLevelUpEffect() {
        Random random = new Random();
        for (int i = 0; i < 16; i++) {
            Circle particle = new Circle(3 + random.nextDouble() * 3, Color.web(i % 2 == 0 ? "#f7d774" : "#8ce4ff"));
            particle.setManaged(false);
            particle.setTranslateX(550 + random.nextInt(80) - 40);
            particle.setTranslateY(280 + random.nextInt(30) - 15);
            stackPane.getChildren().add(particle);

            TranslateTransition rise = new TranslateTransition(Duration.millis(750 + random.nextInt(350)), particle);
            rise.setByY(-70 - random.nextInt(80));
            rise.setByX(random.nextInt(90) - 45);

            FadeTransition fade = new FadeTransition(Duration.millis(850 + random.nextInt(250)), particle);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);

            ParallelTransition transition = new ParallelTransition(rise, fade);
            transition.setOnFinished(event -> stackPane.getChildren().remove(particle));
            transition.play();
        }
    }

    /**
     * Versieht ein Diagramm mit dem einheitlichen Stil.
     *
     * @param chart das zu gestaltende Diagramm
     */
    private void styleHabiticaChart(javafx.scene.chart.Chart chart) {
        chart.getStyleClass().add("habitica-chart-card");
        Platform.runLater(() -> {
            chart.applyCss();
            for (Node node : chart.lookupAll(".chart-title, .axis-label, .axis .tick-label, .chart-legend-item, .chart-pie-label")) {
                node.setStyle("-fx-text-fill: #f0e6ff; -fx-fill: #f0e6ff;");
            }
        });
    }

    /**
     * Weist den Datenreihen der Dashboard-Diagramme die vorgesehenen Farben zu.
     */
    private void applyHabiticaSeriesColors() {
        Platform.runLater(() -> {
            if (dashboardCompletionRateSeries != null && dashboardCompletionRateSeries.getChart() != null) {
                for (Node line : dashboardCompletionRateSeries.getChart().lookupAll(".default-color0.chart-series-line")) {
                    line.setStyle("-fx-stroke: #6bd7ff; -fx-stroke-width: 2.4px;");
                }
            }
            if (dashboardCategoryStrengthSeries != null && dashboardCategoryStrengthSeries.getChart() != null) {
                for (Node bar : dashboardCategoryStrengthSeries.getChart().lookupAll(".default-color0.chart-bar")) {
                    bar.setStyle("-fx-bar-fill: #f5c451;");
                }
            }
            if (dashboardStreakSeries != null && dashboardStreakSeries.getChart() != null) {
                for (Node bar : dashboardStreakSeries.getChart().lookupAll(".default-color0.chart-bar")) {
                    bar.setStyle("-fx-bar-fill: #8e6bff;");
                }
            }
            if (dashboardXpAreaSeries != null && dashboardXpAreaSeries.getChart() != null) {
                for (Node line : dashboardXpAreaSeries.getChart().lookupAll(".default-color0.chart-series-area-line")) {
                    line.setStyle("-fx-stroke: #ff8a65; -fx-stroke-width: 2px;");
                }
                for (Node fill : dashboardXpAreaSeries.getChart().lookupAll(".default-color0.chart-series-area-fill")) {
                    fill.setStyle("-fx-fill: rgba(255, 138, 101, 0.35);");
                }
            }
        });
    }

    /**
     * Ermittelt den im Dashboard gewählten Zeitraum in Tagen.
     *
     * @return die Anzahl der Tage
     */
    private int getSelectedRangeDays() {
        if (dashboardRangeSelector == null) {
            return 7;
        }
        String selected = dashboardRangeSelector.getSelectionModel().getSelectedItem();
        if ("Last 30 Days".equals(selected)) {
            return 30;
        }
        if ("Last Year".equals(selected)) {
            return 365;
        }
        return 7;
    }

    /**
     * Erzeugt die Daten für das Erfüllungsquoten-Diagramm.
     *
     * @param days der betrachtete Zeitraum in Tagen
     * @return die Diagrammdaten (Beschriftung zu Wert)
     */
    private Map<String, Integer> createCompletionRateData(int days) {
        Map<String, Integer> data = new LinkedHashMap<>();
        int totalHabits = Math.max(1, mainController.getUser().getTasks().size());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(days <= 30 ? "MM-dd" : "MMM");
        for (int daysAgo = days - 1; daysAgo >= 0; daysAgo--) {
            LocalDate day = LocalDate.now().minusDays(daysAgo);
            int completed = mainController.getUser().getCompletionCountForDate(day);
            int rate = (int) Math.round((completed * 100.0) / totalHabits);
            data.put(day.format(formatter), rate);
        }
        return data;
    }

    /**
     * Erzeugt die Daten für das Kategorien-Stärke-Diagramm.
     *
     * @return die Diagrammdaten (Kategorie zu Wert)
     */
    private Map<String, Integer> createCategoryStrengthData() {
        int dailyStrength = 0;
        int weeklyStrength = 0;
        int oneTimeStrength = 0;
        for (AbstractTask task : mainController.getUser().getTasks()) {
            if (task instanceof DailyHabit) {
                dailyStrength += task.calculatePoints();
            } else if (task instanceof WeeklyHabit) {
                weeklyStrength += task.calculatePoints();
            } else {
                oneTimeStrength += task.calculatePoints();
            }
        }
        Map<String, Integer> data = new LinkedHashMap<>();
        data.put("Daily", dailyStrength);
        data.put("Weekly", weeklyStrength);
        data.put("OneTime", oneTimeStrength);
        return data;
    }

    /**
     * Erzeugt die Daten für das Serien-Diagramm.
     *
     * @return die Diagrammdaten (Gewohnheit zu Serie)
     */
    private Map<String, Integer> createStreakData() {
        Map<String, Integer> data = new LinkedHashMap<>();
        int limit = 10;
        int count = 0;
        for (AbstractTask task : mainController.getUser().getTasksSortedByStreak()) {
            if (count >= limit) {
                break;
            }
            if (task.getStreak() > 0) {
                data.put(task.getName(), task.getStreak());
                count++;
            }
        }
        return data;
    }

    /**
     * Startet die regelmäßige automatische Aktualisierung des Dashboards.
     */
    private void startLiveDashboardRefresh() {
        if (dashboardRefreshTimeline != null) {
            dashboardRefreshTimeline.stop();
        }

        dashboardRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> refreshDashboardData()));
        dashboardRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        dashboardRefreshTimeline.play();
    }

    /**
     * Aktualisiert die Dashboard-Daten (auf dem JavaFX-Thread).
     */
    public void refreshDashboardData() {
        if (dashboardCompletionRateSeries == null
                || dashboardCategoryStrengthSeries == null
                || dashboardStreakSeries == null
                || dashboardXpAreaSeries == null) {
            refreshStatsData();
            return;
        }
        refreshDashboardDataInternal();
        refreshStatsData();
    }

    /**
     * Berechnet und setzt die Diagrammdaten des Dashboards neu.
     */
    private void refreshDashboardDataInternal() {
        int days = getSelectedRangeDays();
        Map<String, Integer> completionRateData = createCompletionRateData(days);
        dashboardCompletionRateSeries.getData().clear();
        for (Map.Entry<String, Integer> entry : completionRateData.entrySet()) {
            dashboardCompletionRateSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        Map<String, Integer> categoryStrengthData = createCategoryStrengthData();
        dashboardCategoryStrengthSeries.getData().clear();
        for (Map.Entry<String, Integer> entry : categoryStrengthData.entrySet()) {
            dashboardCategoryStrengthSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        Map<String, Integer> streakData = createStreakData();
        dashboardStreakSeries.getData().clear();
        for (Map.Entry<String, Integer> entry : streakData.entrySet()) {
            dashboardStreakSeries.getData().add(new XYChart.Data<>(entry.getValue(), entry.getKey()));
        }

        List<Integer> xpWindow = mainController.getUser().getXpHistoryWindow(days);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(days <= 30 ? "MM-dd" : "MMM");
        dashboardXpAreaSeries.getData().clear();
        for (int index = 0; index < xpWindow.size(); index++) {
            LocalDate day = LocalDate.now().minusDays(days - 1L - index);
            String label = day.format(formatter);
            dashboardXpAreaSeries.getData().add(new XYChart.Data<>(label, xpWindow.get(index)));
        }

        applyHabiticaSeriesColors();
    }

    /**
     * Exportiert das aktuelle Dashboard als Bilddatei.
     */
    private void exportDashboardAsImage() {
        if (dashboardContainer == null) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle(t("export.title"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        chooser.setInitialFileName("disciplica-dashboard.png");
        File file = chooser.showSaveDialog(this);
        if (file == null) {
            return;
        }

        try {
            WritableImage image = dashboardContainer.snapshot(new SnapshotParameters(), null);
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, t("export.success.body") + "\n" + file.getAbsolutePath());
            alert.setHeaderText(t("export.success.header"));
            alert.showAndWait();
        } catch (IOException exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR, t("export.failed.body") + " " + exception.getMessage());
            alert.setHeaderText(t("export.failed.header"));
            alert.showAndWait();
        }
    }

    /**
     * Öffnet ein Detailfenster für die ausgewählte Aufgabe (z.&nbsp;B. zum
     * Abschließen).
     */
    public void openNewWindow() {
        Stage subStage = new Stage();
        subStage.setTitle(t("info.window"));
        subStage.initModality(Modality.APPLICATION_MODAL);
        subStage.initOwner(this);

        String[] info = mainController.getInfo();

        nameField.setEditable(false);
        configureHabiticaField(nameField, 210);
        descriptionField.setEditable(false);
        configureHabiticaField(descriptionField, 210);
        pointsField.setEditable(false);
        configureHabiticaField(pointsField, 210);
        typeField.setEditable(false);
        configureHabiticaField(typeField, 210);
        streakField.setEditable(false);
        configureHabiticaField(streakField, 210);
        isCompletedCB = new CheckBox();
        isCompletedCB.getStyleClass().add("habitica-checkbox");
        isCompletedCB.setDisable(true);

        if(!info[0].equals("O")){
            nameField.setText(info[1]);
            descriptionField.setText(info[2]);
            pointsField.setText(info[3]);
            if (info[0].equals("D")) {
                typeField.setText("Daily Habit");
            } else if (info[0].equals("W")) {
                typeField.setText("Weekly Habit");
            }
            else {
                typeField.setText("OneTimeTask");
            }
            streakField.setText(info[5]);
            isCompletedCB.setSelected(info[4].equals("true"));
        }
        else {
            nameField.setText(info[1]);
            descriptionField.setText(info[2]);
            pointsField.setText(info[3]);
            if (info[0].equals("D")) {
                typeField.setText("Daily Habit");
            } else if (info[0].equals("W")) {
                typeField.setText("Weekly Habit");
            }
            else {
                typeField.setText("OneTimeTask");
            }
            isCompletedCB.setSelected(info[4].equals("true"));
        }

        completeButton.addEventHandler(Event.ANY,mainController);
        configureHabiticaButton(completeButton, "success", 210);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(12);
        gridPane.setVgap(12);
        gridPane.add(createFieldLabel(t("info.name")), 0, 1);
        gridPane.add(nameField, 1, 1);
        gridPane.add(createFieldLabel(t("info.description")), 0, 2);
        gridPane.add(descriptionField, 1, 2);
        gridPane.add(createFieldLabel(t("info.points")), 0, 3);
        gridPane.add(pointsField, 1, 3);
        gridPane.add(createFieldLabel(t("info.type")), 0, 4);
        gridPane.add(typeField, 1, 4);
        if(!info[0].equals("O")) {
            gridPane.add(createFieldLabel(t("info.streak")), 0, 5);
            gridPane.add(streakField, 1, 5);
            gridPane.add(createFieldLabel(t("info.completed")), 0, 6);
            gridPane.add(isCompletedCB, 1, 6);
        }
        else {
            gridPane.add(createFieldLabel(t("info.completed")), 0, 5);
            gridPane.add(isCompletedCB, 1, 5);
        }
        gridPane.add(completeButton, 0, 7, 2, 1);


        VBox vbox = createPanel(t("info.window"), gridPane);
        vbox.setPadding(new Insets(20));
        Scene scene = new Scene(vbox, 390, 430);
        var css = getClass().getResource("/css/habitica-theme.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }


        subStage.setScene(scene);
        applyAccessibility(vbox);
        subStage.show();
    }

    /**
     * Lädt die Sprachressourcen (Texte) für die Oberfläche.
     *
     * @return das geladene Ressourcenbündel oder {@code null}, falls keines
     *         gefunden wird
     */
    private ResourceBundle loadBundle() {
        try {
            return ResourceBundle.getBundle("i18n.messages", Locale.getDefault());
        } catch (MissingResourceException exception) {
            return ResourceBundle.getBundle("i18n.messages", Locale.ENGLISH);
        }
    }

    /**
     * Liefert den übersetzten Text zu einem Schlüssel; fehlt der Schlüssel,
     * wird er selbst zurückgegeben.
     *
     * @param key der Textschlüssel
     * @return der übersetzte Text
     */
    private String t(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException exception) {
            return key;
        }
    }

    /**
     * Richtet die globalen Tastenkürzel des Fensters ein.
     *
     * @param scene die Szene, für die die Kürzel gelten
     */
    private void configureGlobalKeyboardShortcuts(Scene scene) {
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN), dashboardBTN::fire);
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN), habitsBTN::fire);
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN), statsBTN::fire);
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN), () -> {
            if (saveButton != null && saveButton.getScene() != null) {
                saveButton.fire();
            }
        });
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.A, KeyCombination.CONTROL_DOWN), () -> {
            if (addButton != null && addButton.getScene() != null) {
                addButton.fire();
            }
        });
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN), () -> {
            if (removeButton != null && removeButton.getScene() != null) {
                removeButton.fire();
            }
        });
        scene.getAccelerators().put(new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN), () -> {
            if (changeButton != null && changeButton.getScene() != null) {
                changeButton.fire();
            }
        });
    }

    /**
     * Setzt rekursiv Hilfen zur Barrierefreiheit (z.&nbsp;B. beschreibende
     * Texte) für einen Knoten und alle seine Kindelemente.
     *
     * @param root der Wurzelknoten, ab dem die Hilfen gesetzt werden
     */
    private void applyAccessibility(Node root) {
        if (root == null) {
            return;
        }
        if (root.focusTraversableProperty() != null && !root.focusTraversableProperty().isBound()) {
            root.setFocusTraversable(true);
        }
        if (root instanceof Labeled labeled) {
            String text = labeled.getText();
            if (text != null && !text.isBlank()) {
                labeled.setAccessibleText(text);
            }
        }
        if (root instanceof TextInputControl input) {
            String prompt = input.getPromptText();
            if (prompt != null && !prompt.isBlank()) {
                input.setAccessibleText(prompt);
            }
        }
        if (root instanceof Parent parent) {
            for (Node child : parent.getChildrenUnmodifiable()) {
                applyAccessibility(child);
            }
        }
    }
}
