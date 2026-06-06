package View;

import com.google.inject.Injector;
import javafx.application.Platform;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
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

public class View extends Stage {

    final Button dashboardBTN = new Button();
    final Button habitsBTN = new Button();
    final Button statsBTN = new Button();

    MainController mainController;

    final Button[] buttonsLMenu = {dashboardBTN, habitsBTN, statsBTN};

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
    private int lastKnownLevel = -1;
    private String equippedWeapon = "None";
    private String equippedArmor = "None";
    private String equippedHeadgear = "None";
    private String selectedSkinTone = "Light";
    private String selectedHairStyle = "Short";
    private String selectedHairColor = "Black";
    private String selectedBeardStyle = "None";
    private final ResourceBundle bundle;

    public ListView<String> getListViewTasks() {
        return listViewTasks;
    }

    public View() {
        throw new IllegalStateException("Use DI-enabled constructor");
    }

    public View(Injector injector) {
        this.bundle = loadBundle();
        dashboardBTN.setText("D");
        habitsBTN.setText("H");
        statsBTN.setText("S");
        completeButton.setText(t("habits.complete"));
        saveButton.setText(t("habits.save"));

        UserService userService = injector.getInstance(UserService.class);
        mainController = new MainController(this, userService);

        Scene scene = new Scene(hbox, 1100, 600);
        var css = getClass().getResource("/css/habitica-theme.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        setTitle(t("app.title"));
        if (Properties.applicationImageIconAsICO != null) {
            getIcons().add(Properties.applicationImageIconAsICO);
        }

        VBox leftMenu = new VBox(12);
        leftMenu.setPadding(new Insets(20, 10, 20, 10));
        leftMenu.setPrefWidth(76);
        leftMenu.setMinWidth(76);
        leftMenu.setStyle("-fx-background-color: #17142b; -fx-border-color: transparent #3d3d66 transparent transparent;");

        Label navTitle = new Label("D");
        navTitle.setStyle("-fx-text-fill: #f5e03a; -fx-font-size: 22px; -fx-font-weight: bold; -fx-padding: 8 0 14 0;");
        navTitle.setMaxWidth(Double.MAX_VALUE);
        navTitle.setAlignment(Pos.CENTER);

        for (Button b : buttonsLMenu) {
            b.setPrefSize(56, 56);
            b.setFocusTraversable(false);
            styleNavButton(b, false);
            b.setOnMouseEntered(e -> {
                if (b != dashboardBTN && b != habitsBTN && b != statsBTN) {
                    return;
                }
                if (!b.getStyle().contains("#4f2a93")) {
                    b.setStyle("-fx-background-color: #26224a; -fx-background-radius: 8; -fx-border-radius: 8; "
                            + "-fx-border-color: #3d3d66; -fx-border-width: 1; -fx-text-fill: #e7dcff; -fx-font-size: 16px; -fx-font-weight: bold;");
                }
            });
            b.setOnMouseExited(e -> {
                if (b != dashboardBTN && b != habitsBTN && b != statsBTN) {
                    return;
                }
                if (!b.getStyle().contains("#4f2a93")) {
                    styleNavButton(b, false);
                }
            });
            b.addEventHandler(Event.ANY,mainController);
        }
        dashboardBTN.setTooltip(new Tooltip("Dashboard"));
        habitsBTN.setTooltip(new Tooltip("Habits"));
        statsBTN.setTooltip(new Tooltip("Stats"));
        leftMenu.getChildren().addAll(navTitle, dashboardBTN, habitsBTN, statsBTN);

        hbox.getChildren().add(leftMenu);

        this.setOnCloseRequest(event -> {
            event.consume();
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
    }

    public void openHabitMenu() throws IOException {

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25, 25, 25, 25));

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
        addButton.setStyle("-fx-background-color: #4fc05f;-fx-background-radius: 5px;-fx-border-color: #2f7538; -fx-border-width: 1px;-fx-border-radius: 5px;-fx-text-fill: white;-fx-font-weight: bold;");
        addButton.setOnMouseEntered(e -> addButton.setStyle("-fx-background-color: #4fc05f;-fx-background-radius: 5px;-fx-border-color: #2f7538; -fx-border-width: 1px;-fx-border-radius: 5px; -fx-effect: dropshadow(gaussian, #4fc05f, 10, 0.5, 0, 0);-fx-text-fill: white;-fx-font-weight: bold;"));
        addButton.setOnMouseExited(e -> addButton.setStyle("-fx-background-color: #4fc05f;-fx-background-radius: 5px;-fx-border-color: #2f7538; -fx-border-width: 1px;-fx-border-radius: 5px;-fx-text-fill: white;-fx-font-weight: bold;"));
        addButton.setPrefSize(125, 40);
        removeButton = new Button("Delete Habit");
        removeButton.addEventHandler(Event.ANY,mainController);
        removeButton.setStyle("-fx-background-color: #e14242;-fx-background-radius: 5px;-fx-border-color: #911010; -fx-border-width: 1px;-fx-border-radius: 5px;-fx-text-fill: white;-fx-font-weight: bold;");
        removeButton.setOnMouseEntered(e -> removeButton.setStyle("-fx-background-color: #e14242;-fx-background-radius: 5px;-fx-border-color: #911010; -fx-border-width: 1px;-fx-border-radius: 5px; -fx-effect: dropshadow(gaussian, #e14242, 10, 0.5, 0, 0);-fx-text-fill: white;-fx-font-weight: bold;"));
        removeButton.setOnMouseExited(e -> removeButton.setStyle("-fx-background-color: #e14242;-fx-background-radius: 5px;-fx-border-color: #911010; -fx-border-width: 1px;-fx-border-radius: 5px;-fx-text-fill: white;-fx-font-weight: bold;"));
        removeButton.setPrefSize(125, 40);
        changeButton = new Button("Update Selected");
        changeButton.addEventHandler(Event.ANY,mainController);
        changeButton.setStyle("-fx-background-color: #238ac9;-fx-background-radius: 5px;-fx-border-color: #1251af; -fx-border-width: 1px;-fx-border-radius: 5px;-fx-text-fill: white;-fx-font-weight: bold;");
        changeButton.setOnMouseEntered(e -> changeButton.setStyle("-fx-background-color: #238ac9;-fx-background-radius: 5px;-fx-border-color: #1251af; -fx-border-width: 1px;-fx-border-radius: 5px; -fx-effect: dropshadow(gaussian, #238ac9, 10, 0.5, 0, 0);-fx-text-fill: white;-fx-font-weight: bold;"));
        changeButton.setOnMouseExited(e -> changeButton.setStyle("-fx-background-color: #62a0c5;-fx-background-radius: 5px;-fx-border-color: #1251af; -fx-border-width: 1px;-fx-border-radius: 5px;-fx-text-fill: white;-fx-font-weight: bold;"));
        changeButton.setPrefSize(125, 40);
        saveButton.setText("Save Data");
        saveButton.addEventHandler(Event.ANY, mainController);
        saveButton.setStyle("-fx-background-color: #7c5ee6;-fx-background-radius: 5px;-fx-border-color: #5132b8; -fx-border-width: 1px;-fx-border-radius: 5px;-fx-text-fill: white;-fx-font-weight: bold;");
        saveButton.setOnMouseEntered(e -> saveButton.setStyle("-fx-background-color: #7c5ee6;-fx-background-radius: 5px;-fx-border-color: #5132b8; -fx-border-width: 1px;-fx-border-radius: 5px; -fx-effect: dropshadow(gaussian, #7c5ee6, 10, 0.5, 0, 0);-fx-text-fill: white;-fx-font-weight: bold;"));
        saveButton.setOnMouseExited(e -> saveButton.setStyle("-fx-background-color: #7c5ee6;-fx-background-radius: 5px;-fx-border-color: #5132b8; -fx-border-width: 1px;-fx-border-radius: 5px;-fx-text-fill: white;-fx-font-weight: bold;"));
        saveButton.setPrefSize(125, 40);

        FlowPane listViewP = new FlowPane();
        listViewP.setPadding(new Insets(25,0,0,0));
        listViewP.setOrientation(Orientation.VERTICAL);


        itemsObservable = FXCollections.observableArrayList(mainController.getHabits());
        listViewTasks = new ListView<>(itemsObservable);
        listViewTasks.getSelectionModel().selectedItemProperty().addListener(mainController);
        listViewTasks.getSelectionModel().selectFirst();
        listViewTasks.addEventHandler(Event.ANY,mainController);
        listViewTasks.setFixedCellSize(28);
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
                content.setSpacing(2);
                content.getChildren().addAll(title, subtitle);
                HBox.setHgrow(content, Priority.ALWAYS);
                row.setSpacing(8);
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
        listViewTasks.setPrefWidth(350);
        listViewTasks.setPrefHeight(500);
        listViewTasks.setStyle("-fx-background-color: #e4ebff;-fx-border-color: #7f7fab; -fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;-fx-text-fill: white;-fx-font-size: 15px;");

        comboBox = new ComboBox<>();
        String[] types = {"Daily Habit", "Weekly Habit", "OneTimeTask"};
        comboBox.getItems().addAll(types);
        comboBox.setStyle(
                "-fx-background-color: #315184;" +
                        "-fx-background-radius: 5px;" +
                        "-fx-border-color: #4a6691;" +
                        "-fx-border-radius: 5px;" +
                        "-fx-min-width: 200px;" +
                        "-fx-pref-height: 40px;"+
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 15px;"+
                        "-fx-border-width: 2px;"
        );
        comboBox.getSelectionModel().selectFirst();
        comboBox.setStyle(
                "-fx-background-color: #315184;" +
                        "-fx-background-radius: 5px;" +
                        "-fx-border-color: #4a6691;" +
                        "-fx-border-radius: 5px;" +
                        "-fx-min-width: 200px;" +
                        "-fx-pref-height: 40px;" +
                        "-fx-border-width: 2px;"
        );

        comboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Arial'; -fx-font-size: 14px;");
                }
            }
        });

        comboBox.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                comboBox.applyCss();
                comboBox.layout();
                Node arrow = comboBox.lookup(".arrow");
                if (arrow != null) {
                    arrow.setStyle("-fx-background-color: #ffffff;");
                }
            }
        });

        comboBox.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: #315184;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #bdbdbd; -fx-background-color: #315184; -fx-font-family: 'Arial';");
                }
            }
        });

        categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().addAll("Health", "Work", "Learning", "Fitness", "Chores");
        categoryComboBox.getSelectionModel().select("Work");
        categoryComboBox.setPrefHeight(40);
        categoryComboBox.setPrefWidth(250);
        categoryComboBox.setStyle("-fx-background-color: #315184;-fx-background-radius: 5px;-fx-border-color: #4a6691;"
                + "-fx-border-radius: 5px;-fx-border-width: 2px;");

        durationSpinner = new Spinner<>(5, 240, 30, 5);
        durationSpinner.setEditable(true);
        durationSpinner.setPrefHeight(40);
        durationSpinner.setPrefWidth(250);
        durationSpinner.getEditor().setStyle("-fx-text-fill: white;");
        durationSpinner.setStyle("-fx-background-color: #315184;-fx-border-color: #4a6691;-fx-border-width: 2px;");


        nameTF.setPrefWidth(250);
        nameTF.setPrefHeight(40);
        nameTF.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        nameTF.setPromptText(t("habits.prompt.name"));
        descriptionTF.setPrefHeight(40);
        descriptionTF.setPrefWidth(250);
        descriptionTF.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        descriptionTF.setPromptText(t("habits.prompt.description"));
        pointsTF.setPrefHeight(40);
        pointsTF.setPrefWidth(250);
        pointsTF.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        pointsTF.setPromptText(t("habits.prompt.points"));
        pointsTF.setEditable(false);
        streakField.setPrefHeight(40);
        streakField.setPrefWidth(250);
        streakField.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        comboBox.setPrefHeight(40);
        comboBox.setPrefWidth(250);



        Label format = new Label(t("habits.list"));
        format.setStyle("-fx-font-size: 25px;");
        listViewP.getChildren().addAll(format,listViewTasks);

        FlowPane controlButtons = new FlowPane();
        controlButtons.setHgap(10);
        controlButtons.setPadding(new Insets(10,0,0,0));

        controlButtons.getChildren().addAll(addButton,removeButton,changeButton,saveButton);

        Label head = new Label(t("habits.title"));
        head.setStyle("-fx-font-size: 25px;-fx-font-style: bold;");
        gridPane.add(head,0,13);

        Label l1 = new Label(t("habits.name"));
        l1.setStyle("-fx-font-size: 15px");
        gridPane.add(l1, 0, 14);
        gridPane.add(nameTF, 1, 14);

        Label l2 = new Label(t("habits.description"));
        l2.setStyle("-fx-font-size: 15px");
        gridPane.add(l2, 0, 15);
        gridPane.add(descriptionTF, 1, 15);

        Label l3 = new Label("XP (auto)");
        l3.setStyle("-fx-font-size: 15px");
        gridPane.add(l3, 0, 16);
        gridPane.add(pointsTF, 1, 16);

        Label l4 = new Label("Category");
        l4.setStyle("-fx-font-size: 15px");
        gridPane.add(l4, 0, 17);
        gridPane.add(categoryComboBox, 1, 17);

        Label l5 = new Label("Est. Duration (min)");
        l5.setStyle("-fx-font-size: 15px");
        gridPane.add(l5, 0, 18);
        gridPane.add(durationSpinner, 1, 18);

        Label l6 = new Label(t("habits.type"));
        l6.setStyle("-fx-font-size: 15px");
        gridPane.add(l6, 0, 19);
        gridPane.add(comboBox, 1, 19);
        gridPane.add(controlButtons, 0, 20, 2, 1);

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

        HBox contentWrapper = new HBox(20);
        contentWrapper.getChildren().addAll(gridPane, listViewP);


        stackPane.getChildren().setAll(contentWrapper);
        setActiveNav(habitsBTN);
        applyAccessibility(stackPane);
    }

    public void showLoading(String message) {
        if (loadingLabel != null) {
            loadingLabel.setText(message == null || message.isBlank() ? "Loading..." : message);
        }
        if (loadingOverlay != null && !stackPane.getChildren().contains(loadingOverlay)) {
            loadingOverlay.setMouseTransparent(false);
            stackPane.getChildren().add(loadingOverlay);
        }
    }

    public void hideLoading() {
        if (loadingOverlay != null) {
            stackPane.getChildren().remove(loadingOverlay);
        }
    }

    private void initializeLoadingOverlay() {
        ProgressIndicator indicator = new ProgressIndicator();
        loadingLabel = new Label("Loading...");
        loadingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        VBox box = new VBox(12, indicator, loadingLabel);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: rgba(36, 26, 58, 0.92); -fx-background-radius: 8px;");

        loadingOverlay = new StackPane(box);
        loadingOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.35);");
        loadingOverlay.setPickOnBounds(true);
        loadingOverlay.prefWidthProperty().bind(stackPane.widthProperty());
        loadingOverlay.prefHeightProperty().bind(stackPane.heightProperty());
    }

    public void openDashboard(){
        stackPane.getChildren().clear();
        VBox container = new VBox(14);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #241a3a;");
        dashboardContainer = container;

        Label header = new Label(t("nav.dashboard"));
        header.setStyle("-fx-font-size: 30px; -fx-text-fill: #f0e6ff; -fx-font-weight: bold;");

        Label subtitle = new Label(t("dashboard.subtitle"));
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #c9b9ea;");

        HBox dashboardToolbar = new HBox(10);
        dashboardToolbar.setAlignment(Pos.CENTER_LEFT);
        Label rangeLabel = new Label(t("dashboard.range"));
        rangeLabel.setStyle("-fx-text-fill: #f0e6ff;");
        dashboardRangeSelector = new ComboBox<>();
        dashboardRangeSelector.getItems().setAll(t("dashboard.range.7"), t("dashboard.range.30"), t("dashboard.range.year"));
        dashboardRangeSelector.getSelectionModel().selectFirst();
        dashboardRangeSelector.setStyle("-fx-background-color: #372b61; -fx-text-fill: white;");
        dashboardRangeSelector.valueProperty().addListener((obs, oldValue, newValue) -> refreshDashboardData());
        Button exportButton = new Button(t("dashboard.export"));
        exportButton.setStyle("-fx-background-color: #7c5ee6; -fx-text-fill: white; -fx-font-weight: bold;");
        exportButton.setOnAction(event -> exportDashboardAsImage());
        dashboardToolbar.getChildren().addAll(rangeLabel, dashboardRangeSelector, exportButton);

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("MM-dd");

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

        VBox.setVgrow(chartGrid, Priority.ALWAYS);
        container.getChildren().addAll(header, subtitle, dashboardToolbar, chartGrid);
        container.setAlignment(Pos.TOP_LEFT);
        stackPane.getChildren().add(container);

        refreshDashboardData();
        startLiveDashboardRefresh();
        setActiveNav(dashboardBTN);
        applyAccessibility(stackPane);
    }

    public void openStats(){
        stackPane.getChildren().clear();
        HBox statsLayout = new HBox(22);
        statsLayout.setPadding(new Insets(24));
        statsLayout.setStyle("-fx-background-color: #241a3a;");
        GridPane gridPane = new GridPane();

        usernameTF = new TextField();
        usernameTF.setEditable(false);
        usernameTF.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        levelTF = new TextField();
        levelTF.setEditable(false);
        levelTF.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        titelTF = new TextField();
        titelTF.setEditable(false);
        titelTF.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        expirienceTF = new TextField();
        expirienceTF.setEditable(false);
        expirienceTF.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        goldTF = new TextField();
        goldTF.setEditable(false);
        goldTF.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        healthTF = new TextField();
        healthTF.setEditable(false);
        healthTF.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        xpProgressBar = new ProgressBar(0);
        xpProgressBar.setId("xpProgressBar");
        xpProgressBar.setPrefWidth(250);
        xpProgressBar.setStyle("-fx-accent: #6bd7ff; -fx-control-inner-background: #1d1731;");
        hpProgressBar = new ProgressBar(0);
        hpProgressBar.setPrefWidth(250);
        hpProgressBar.setStyle("-fx-accent: #ff6b8e; -fx-control-inner-background: #1d1731;");

        refreshStatsData();

        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25, 25, 25, 25));
        gridPane.add(new Label(t("stats.username")), 0, 1);
        gridPane.add(usernameTF, 1, 1);
        gridPane.add(new Label(t("stats.level")), 0, 2);
        gridPane.add(levelTF, 1, 2);
        gridPane.add(new Label(t("stats.title")), 0, 3);
        gridPane.add(titelTF, 1, 3);
        gridPane.add(new Label(t("stats.experience")), 0, 4);
        gridPane.add(expirienceTF, 1, 4);
        gridPane.add(new Label(t("stats.gold")), 0, 5);
        gridPane.add(goldTF, 1, 5);
        gridPane.add(new Label(t("stats.health")), 0, 6);
        gridPane.add(healthTF, 1, 6);
        gridPane.add(new Label(t("stats.xpProgress")), 0, 7);
        gridPane.add(xpProgressBar, 1, 7);
        gridPane.add(new Label(t("stats.hpProgress")), 0, 8);
        gridPane.add(hpProgressBar, 1, 8);

        Label panelTitle = new Label(t("stats.player"));
        panelTitle.setStyle("-fx-text-fill: #f4ecff; -fx-font-size: 26px; -fx-font-weight: bold;");
        VBox playerPanel = new VBox(12, panelTitle, gridPane);
        playerPanel.setStyle("-fx-background-color: #2d2350; -fx-background-radius: 8; -fx-padding: 16;");
        playerPanel.setPrefWidth(420);

        VBox avatarAndShop = new VBox(18, createAvatarPanel(), createShopPanel());
        avatarAndShop.setPrefWidth(520);
        statsLayout.getChildren().addAll(playerPanel, avatarAndShop);
        stackPane.getChildren().add(statsLayout);
        setActiveNav(statsBTN);
        applyAccessibility(stackPane);
    }

    private void styleNavButton(Button button, boolean active) {
        if (active) {
            button.setStyle("-fx-background-color: #4f2a93; -fx-background-radius: 8; -fx-border-radius: 8; "
                    + "-fx-border-color: #9a62ff; -fx-border-width: 1; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
            return;
        }
        button.setStyle("-fx-background-color: #1f1b3d; -fx-background-radius: 8; -fx-border-radius: 8; "
                + "-fx-border-color: #3d3d66; -fx-border-width: 1; -fx-text-fill: #c3acff; -fx-font-size: 16px; -fx-font-weight: bold;");
    }

    private void setActiveNav(Button activeButton) {
        for (Button button : buttonsLMenu) {
            styleNavButton(button, button == activeButton);
        }
    }

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

    private VBox createAvatarPanel() {
        Label title = new Label(t("stats.avatar"));
        title.setStyle("-fx-text-fill: #f4ecff; -fx-font-size: 20px; -fx-font-weight: bold;");

        StackPane avatarCanvas = new StackPane();
        avatarCanvas.setPrefSize(240, 240);
        avatarCanvas.setStyle("-fx-background-color: #1d1731; -fx-background-radius: 10; -fx-border-color: #4c3d78; -fx-border-radius: 10;");
        avatarPixelCanvas = new Canvas(340, 220);
        avatarCanvas.getChildren().add(avatarPixelCanvas);
        renderAvatarPixelArt();

        GridPane customizer = new GridPane();
        customizer.setHgap(10);
        customizer.setVgap(8);

        skinToneSelector = createAvatarCombo("Light", "Tan", "Brown", "Dark");
        skinToneSelector.setValue(selectedSkinTone);
        skinToneSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedSkinTone = newVal;
                renderAvatarPixelArt();
            }
        });

        hairStyleSelector = createAvatarCombo("Short", "Mohawk", "Long");
        hairStyleSelector.setValue(selectedHairStyle);
        hairStyleSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedHairStyle = newVal;
                renderAvatarPixelArt();
            }
        });

        hairColorSelector = createAvatarCombo("Black", "Brown", "Blonde", "Red", "White");
        hairColorSelector.setValue(selectedHairColor);
        hairColorSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedHairColor = newVal;
                renderAvatarPixelArt();
            }
        });

        beardStyleSelector = createAvatarCombo("None", "Goatee", "Full");
        beardStyleSelector.setValue(selectedBeardStyle);
        beardStyleSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedBeardStyle = newVal;
                renderAvatarPixelArt();
            }
        });

        customizer.add(new Label("Skin"), 0, 0);
        customizer.add(skinToneSelector, 1, 0);
        customizer.add(new Label("Hair"), 0, 1);
        customizer.add(hairStyleSelector, 1, 1);
        customizer.add(new Label("Hair Color"), 2, 0);
        customizer.add(hairColorSelector, 3, 0);
        customizer.add(new Label("Beard"), 2, 1);
        customizer.add(beardStyleSelector, 3, 1);

        VBox panel = new VBox(10, title, avatarCanvas, customizer);
        panel.setStyle("-fx-background-color: #2d2350; -fx-background-radius: 8; -fx-padding: 14;");
        return panel;
    }

    private ComboBox<String> createAvatarCombo(String... values) {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(values);
        combo.setStyle("-fx-background-color: #3a2f66; -fx-border-color: #6b53cf; -fx-border-radius: 4; "
                + "-fx-background-radius: 4; -fx-text-fill: #f4ecff; -fx-font-size: 13px;");
        combo.setPrefWidth(120);
        combo.setVisibleRowCount(6);
        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-background-color: #3a2f66; -fx-text-fill: #f4ecff; -fx-font-size: 13px;");
            }
        });
        combo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item);
                setStyle("-fx-background-color: #2a214a; -fx-text-fill: #f4ecff; -fx-font-size: 13px;");
            }
        });
        return combo;
    }

    private VBox createShopPanel() {
        Label title = new Label(t("stats.shop"));
        title.setStyle("-fx-text-fill: #f4ecff; -fx-font-size: 20px; -fx-font-weight: bold;");

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
        panel.setStyle("-fx-background-color: #2d2350; -fx-background-radius: 8; -fx-padding: 14;");
        return panel;
    }

    private Button createShopButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: #6b53cf; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6;");
        button.setPrefWidth(180);
        return button;
    }

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

    private void updateAvatarLayers() {
        if (avatarPixelCanvas == null) {
            return;
        }
        renderAvatarPixelArt();
    }

    private void renderAvatarPixelArt() {
        if (avatarPixelCanvas == null) {
            return;
        }

        GraphicsContext gc = avatarPixelCanvas.getGraphicsContext2D();
        double width = avatarPixelCanvas.getWidth();
        double height = avatarPixelCanvas.getHeight();

        gc.setFill(Color.web("#1d1731"));
        gc.fillRect(0, 0, width, height);

        int p = 6;
        int ox = 86;
        int oy = 8;

        Color skin = resolveSkinColor(selectedSkinTone);
        Color hair = resolveHairColor(selectedHairColor);
        Color beard = hair.darker();

        // cape sits behind the body
        if ("Cape".equals(equippedArmor)) {
            gc.setFill(Color.web("#704fd1"));
            gc.fillRect(ox + 4 * p, oy + 10 * p, 20 * p, 18 * p);
            gc.setFill(Color.web("#5a3cad"));
            gc.fillRect(ox + 6 * p, oy + 12 * p, 16 * p, 15 * p);
            gc.setFill(Color.web("#8a6bf2"));
            gc.fillRect(ox + 9 * p, oy + 10 * p, 10 * p, 2 * p);
        }

        // Base avatar body
        gc.setFill(skin);
        gc.fillRect(ox + 9 * p, oy + 8 * p, 10 * p, 8 * p); // face
        gc.fillRect(ox + 8 * p, oy + 16 * p, 2 * p, 3 * p); // left hand
        gc.fillRect(ox + 18 * p, oy + 16 * p, 2 * p, 3 * p); // right hand

        drawHair(gc, ox, oy, p, hair);
        drawBeard(gc, ox, oy, p, beard);

        gc.setFill(Color.web("#2b2b3d"));
        gc.fillRect(ox + 11 * p, oy + 11 * p, p, p); // eye L
        gc.fillRect(ox + 16 * p, oy + 11 * p, p, p); // eye R
        gc.setFill(skin.darker());
        gc.fillRect(ox + 14 * p, oy + 12 * p, p, p); // nose
        gc.setFill(Color.web("#7a2f2f"));
        gc.fillRect(ox + 13 * p, oy + 14 * p, 3 * p, p); // mouth
        gc.setFill(Color.web("#4c8bbf"));
        gc.fillRect(ox + 8 * p, oy + 16 * p, 12 * p, 8 * p); // torso
        gc.fillRect(ox + 5 * p, oy + 16 * p, 3 * p, 7 * p);  // arm L
        gc.fillRect(ox + 20 * p, oy + 16 * p, 3 * p, 7 * p); // arm R
        gc.setFill(Color.web("#3a72a3"));
        gc.fillRect(ox + 10 * p, oy + 18 * p, 8 * p, 4 * p);
        gc.setFill(Color.web("#2c3f84"));
        gc.fillRect(ox + 10 * p, oy + 24 * p, 4 * p, 7 * p);  // leg L
        gc.fillRect(ox + 14 * p, oy + 24 * p, 4 * p, 7 * p); // leg R

        // Headgear overlays
        if ("Helm".equals(equippedHeadgear)) {
            gc.setFill(Color.web("#9ec4e6"));
            gc.fillRect(ox + 8 * p, oy + 6 * p, 12 * p, 3 * p);
            gc.fillRect(ox + 9 * p, oy + 9 * p, 10 * p, p);
            gc.setFill(Color.web("#6e8ca8"));
            gc.fillRect(ox + 8 * p, oy + 6 * p, 2 * p, 4 * p);
            gc.fillRect(ox + 18 * p, oy + 6 * p, 2 * p, 4 * p);
        } else if ("Crown".equals(equippedHeadgear)) {
            gc.setFill(Color.web("#ffd45c"));
            gc.fillRect(ox + 9 * p, oy + 6 * p, 10 * p, 2 * p);
            gc.fillRect(ox + 10 * p, oy + 5 * p, 2 * p, p);
            gc.fillRect(ox + 13 * p, oy + 4 * p, 2 * p, 2 * p);
            gc.fillRect(ox + 16 * p, oy + 5 * p, 2 * p, p);
            gc.setFill(Color.web("#d39b28"));
            gc.fillRect(ox + 9 * p, oy + 7 * p, 10 * p, p);
        } else if ("RoyalCrown".equals(equippedHeadgear)) {
            gc.setFill(Color.web("#ffe07c"));
            gc.fillRect(ox + 8 * p, oy + 6 * p, 12 * p, 2 * p);
            gc.fillRect(ox + 9 * p, oy + 5 * p, 2 * p, p);
            gc.fillRect(ox + 12 * p, oy + 4 * p, 2 * p, 2 * p);
            gc.fillRect(ox + 15 * p, oy + 5 * p, 2 * p, p);
            gc.fillRect(ox + 18 * p, oy + 5 * p, p, p);
            gc.setFill(Color.web("#cf9f2c"));
            gc.fillRect(ox + 8 * p, oy + 7 * p, 12 * p, p);
            gc.setFill(Color.web("#8cd7ff"));
            gc.fillRect(ox + 13 * p, oy + 6 * p, p, p);
        }

        // Armor overlays
        if ("Armor".equals(equippedArmor)) {
            gc.setFill(Color.web("#8da6bd"));
            gc.fillRect(ox + 8 * p, oy + 16 * p, 12 * p, 8 * p);
            gc.setFill(Color.web("#5f7388"));
            gc.fillRect(ox + 12 * p, oy + 17 * p, 4 * p, 7 * p);
            gc.setFill(Color.web("#c9d8e6"));
            gc.fillRect(ox + 9 * p, oy + 18 * p, 2 * p, 3 * p);
            gc.fillRect(ox + 17 * p, oy + 18 * p, 2 * p, 3 * p);
        } else if ("DragonArmor".equals(equippedArmor)) {
            gc.setFill(Color.web("#4f2b6f"));
            gc.fillRect(ox + 8 * p, oy + 16 * p, 12 * p, 8 * p);
            gc.setFill(Color.web("#6e3f93"));
            gc.fillRect(ox + 10 * p, oy + 17 * p, 8 * p, 6 * p);
            gc.setFill(Color.web("#c08cff"));
            gc.fillRect(ox + 11 * p, oy + 18 * p, p, 3 * p);
            gc.fillRect(ox + 16 * p, oy + 18 * p, p, 3 * p);
            gc.setFill(Color.web("#2d173d"));
            gc.fillRect(ox + 13 * p, oy + 17 * p, 2 * p, 6 * p);
        }

        // Weapon overlays anchored to right hand
        int handX = ox + 20 * p;
        int handY = oy + 18 * p;
        gc.setFill(skin);
        gc.fillRect(handX, handY, 2 * p, 2 * p);
        if ("Sword".equals(equippedWeapon)) {
            gc.setFill(Color.web("#dfe7ef"));
            gc.fillRect(handX + 2 * p, handY - 6 * p, 2 * p, 8 * p);
            gc.setFill(Color.web("#ffd45c"));
            gc.fillRect(handX + p, handY + p, 4 * p, p);
            gc.setFill(Color.web("#8b5a2b"));
            gc.fillRect(handX + 2 * p, handY + 2 * p, 2 * p, 3 * p);
        } else if ("Axe".equals(equippedWeapon)) {
            gc.setFill(Color.web("#8b5a2b"));
            gc.fillRect(handX + 2 * p, handY - 5 * p, 2 * p, 10 * p);
            gc.setFill(Color.web("#a7b4c6"));
            gc.fillRect(handX - p, handY - 6 * p, 4 * p, 3 * p);
            gc.fillRect(handX - 2 * p, handY - 5 * p, p, 2 * p);
            gc.fillRect(handX + 3 * p, handY - 5 * p, p, p);
        } else if ("Greatsword".equals(equippedWeapon)) {
            gc.setFill(Color.web("#dbe6f3"));
            gc.fillRect(handX + 2 * p, handY - 9 * p, 2 * p, 12 * p);
            gc.setFill(Color.web("#b8c7d8"));
            gc.fillRect(handX + p, handY - 9 * p, 4 * p, p);
            gc.setFill(Color.web("#f0cc58"));
            gc.fillRect(handX, handY + p, 6 * p, p);
            gc.setFill(Color.web("#7a4f24"));
            gc.fillRect(handX + 2 * p, handY + 2 * p, 2 * p, 4 * p);
        } else if ("Halberd".equals(equippedWeapon)) {
            gc.setFill(Color.web("#8b5a2b"));
            gc.fillRect(handX + 2 * p, handY - 10 * p, 2 * p, 15 * p);
            gc.setFill(Color.web("#b9c8d8"));
            gc.fillRect(handX + p, handY - 10 * p, 4 * p, 2 * p);
            gc.fillRect(handX + 4 * p, handY - 9 * p, 2 * p, 3 * p);
            gc.fillRect(handX, handY - 8 * p, p, 2 * p);
        }
    }

    private Color resolveSkinColor(String tone) {
        return switch (tone) {
            case "Tan" -> Color.web("#d6a77f");
            case "Brown" -> Color.web("#a56e45");
            case "Dark" -> Color.web("#70452d");
            default -> Color.web("#f2c8a0");
        };
    }

    private Color resolveHairColor(String colorName) {
        return switch (colorName) {
            case "Brown" -> Color.web("#5a3a29");
            case "Blonde" -> Color.web("#d8bf64");
            case "Red" -> Color.web("#9a3f2e");
            case "White" -> Color.web("#d8d8e8");
            default -> Color.web("#3a4161");
        };
    }

    private void drawHair(GraphicsContext gc, int ox, int oy, int p, Color hair) {
        gc.setFill(hair);
        if ("Mohawk".equals(selectedHairStyle)) {
            gc.fillRect(ox + 13 * p, oy + 4 * p, 2 * p, 5 * p);
            gc.fillRect(ox + 12 * p, oy + 6 * p, 4 * p, 2 * p);
            gc.setFill(hair.brighter());
            gc.fillRect(ox + 13 * p, oy + 5 * p, p, 3 * p);
            return;
        }
        if ("Long".equals(selectedHairStyle)) {
            gc.fillRect(ox + 8 * p, oy + 6 * p, 12 * p, 3 * p);
            gc.fillRect(ox + 8 * p, oy + 9 * p, 2 * p, 5 * p);
            gc.fillRect(ox + 18 * p, oy + 9 * p, 2 * p, 5 * p);
            gc.setFill(hair.brighter());
            gc.fillRect(ox + 10 * p, oy + 7 * p, 8 * p, p);
            return;
        }
        gc.fillRect(ox + 9 * p, oy + 6 * p, 10 * p, 2 * p);
        gc.fillRect(ox + 10 * p, oy + 8 * p, 8 * p, p);
        gc.fillRect(ox + 9 * p, oy + 9 * p, p, p);
        gc.fillRect(ox + 18 * p, oy + 9 * p, p, p);
        gc.setFill(hair.brighter());
        gc.fillRect(ox + 11 * p, oy + 7 * p, 6 * p, p);
    }

    private void drawBeard(GraphicsContext gc, int ox, int oy, int p, Color beard) {
        gc.setFill(beard);
        if ("Goatee".equals(selectedBeardStyle)) {
            gc.fillRect(ox + 13 * p, oy + 15 * p, 2 * p, 2 * p);
            return;
        }
        if ("Full".equals(selectedBeardStyle)) {
            gc.fillRect(ox + 10 * p, oy + 14 * p, 8 * p, 3 * p);
            gc.fillRect(ox + 11 * p, oy + 17 * p, 6 * p, p);
        }
    }

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

    private void styleHabiticaChart(javafx.scene.chart.Chart chart) {
        chart.setStyle("-fx-background-color: #2d2350; -fx-padding: 12; -fx-border-color: #4c3d78; "
                + "-fx-border-radius: 6; -fx-background-radius: 6;");
        Platform.runLater(() -> {
            chart.applyCss();
            for (Node node : chart.lookupAll(".chart-title, .axis-label, .axis .tick-label, .chart-legend-item, .chart-pie-label")) {
                node.setStyle("-fx-text-fill: #f0e6ff; -fx-fill: #f0e6ff;");
            }
        });
    }

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

    private void startLiveDashboardRefresh() {
        if (dashboardRefreshTimeline != null) {
            dashboardRefreshTimeline.stop();
        }

        dashboardRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> refreshDashboardData()));
        dashboardRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        dashboardRefreshTimeline.play();
    }

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

    public void openNewWindow() {
        Stage subStage = new Stage();
        subStage.setTitle(t("info.window"));
        subStage.initModality(Modality.APPLICATION_MODAL);
        subStage.initOwner(this);

        String[] info = mainController.getInfo();

        nameField.setEditable(false);
        nameField.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        descriptionField.setEditable(false);
        descriptionField.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        pointsField.setEditable(false);
        pointsField.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        typeField.setEditable(false);
        typeField.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        streakField.setEditable(false);
        streakField.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        isCompletedCB.setStyle("-fx-text-fill: white;-fx-font-size: 15px;-fx-background-color: white;");
        isCompletedCB = new CheckBox();
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
        completeButton.setPrefSize(125, 40);
        completeButton.setStyle("-fx-background-color: #4fc05f;-fx-background-radius: 5px;-fx-border-color: #2f7538; -fx-border-width: 1px;-fx-border-radius: 5px;-fx-text-fill: white;-fx-font-weight: bold;");
        completeButton.setOnMouseEntered(e -> completeButton.setStyle("-fx-background-color: #4fc05f;-fx-background-radius: 5px;-fx-border-color: #2f7538; -fx-border-width: 1px;-fx-border-radius: 5px; -fx-effect: dropshadow(gaussian, #4fc05f, 10, 0.5, 0, 0);-fx-text-fill: white;-fx-font-weight: bold;"));
        completeButton.setOnMouseExited(e -> completeButton.setStyle("-fx-background-color: #4fc05f;-fx-background-radius: 5px;-fx-border-color: #2f7538; -fx-border-width: 1px;-fx-border-radius: 5px;-fx-text-fill: white;-fx-font-weight: bold;"));

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25, 25, 25, 25));
        gridPane.add(new Label(t("info.name")), 0, 1);
        gridPane.add(nameField, 1, 1);
        gridPane.add(new Label(t("info.description")), 0, 2);
        gridPane.add(descriptionField, 1, 2);
        gridPane.add(new Label(t("info.points")), 0, 3);
        gridPane.add(pointsField, 1, 3);
        gridPane.add(new Label(t("info.type")), 0, 4);
        gridPane.add(typeField, 1, 4);
        if(!info[0].equals("O")) {
            gridPane.add(new Label(t("info.streak")), 0, 5);
            gridPane.add(streakField, 1, 5);
            gridPane.add(new Label(t("info.completed")), 0, 6);
            gridPane.add(isCompletedCB, 1, 6);
        }
        else {
            gridPane.add(new Label(t("info.completed")), 0, 5);
            gridPane.add(isCompletedCB, 1, 5);
        }
        gridPane.add(completeButton, 0, 7, 2, 1);


        VBox vbox = new VBox();
        Scene scene = new Scene(vbox, 325, 375);

        vbox.getChildren().add(gridPane);

        subStage.setScene(scene);
        applyAccessibility(vbox);
        subStage.show();
    }

    private ResourceBundle loadBundle() {
        try {
            return ResourceBundle.getBundle("i18n.messages", Locale.getDefault());
        } catch (MissingResourceException exception) {
            return ResourceBundle.getBundle("i18n.messages", Locale.ENGLISH);
        }
    }

    private String t(String key) {
        try {
            return bundle.getString(key);
        } catch (MissingResourceException exception) {
            return key;
        }
    }

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
