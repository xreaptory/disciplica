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
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
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

public class View extends Stage {

    final Button dashboardBTN = new Button("Dashboard");
    final Button habitsBTN = new Button("Habits");
    final Button statsBTN = new Button("Stats");

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

    final Button completeButton = new Button("Complete");
    final Button saveButton = new Button("Save");
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
    private Label weaponLayerLabel;
    private Label armorLayerLabel;
    private Label headgearLayerLabel;
    private int lastKnownLevel = -1;
    private String equippedWeapon = "None";
    private String equippedArmor = "None";
    private String equippedHeadgear = "None";

    public ListView<String> getListViewTasks() {
        return listViewTasks;
    }

    public View() {
        throw new IllegalStateException("Use DI-enabled constructor");
    }

    public View(Injector injector) {
        UserService userService = injector.getInstance(UserService.class);
        mainController = new MainController(this, userService);

        Scene scene = new Scene(hbox, 1100, 600);
        setTitle("Disciplica");
        if (Properties.applicationImageIconAsICO != null) {
            getIcons().add(Properties.applicationImageIconAsICO);
        }


        FlowPane leftMenu = new FlowPane();

        leftMenu.setPadding(new Insets(200,50,0,50));
        leftMenu.setStyle("-fx-background-color: #bbbbbb");
        leftMenu.setVgap(25);
        leftMenu.setOrientation(Orientation.VERTICAL);
        for (Button b : buttonsLMenu) {
            b.setPrefSize(150, 45);
            b.setStyle("-fx-background-color: #62a0c5;-fx-background-radius: 5px;-fx-border-color: #204170; -fx-border-width: 1px;-fx-border-radius: 5px;");
            b.setOnMouseEntered(e -> b.setStyle("-fx-background-color: #62a0c5;-fx-background-radius: 5px;-fx-border-color: #204170; -fx-border-width: 1px;-fx-border-radius: 5px; -fx-effect: dropshadow(gaussian, #4c8bbf, 10, 0.5, 0, 0);"));
            b.setOnMouseExited(e -> b.setStyle("-fx-background-color: #62a0c5;-fx-background-radius: 5px;-fx-border-color: #204170; -fx-border-width: 1px;-fx-border-radius: 5px;"));
            b.addEventHandler(Event.ANY,mainController);
        }
        leftMenu.getChildren().addAll(dashboardBTN, habitsBTN, statsBTN);

        hbox.getChildren().add(leftMenu);

        this.setOnCloseRequest(event -> {
            event.consume();
            mainController.saveAllAsync(this::close);
        });

        hbox.getChildren().add(stackPane);
        initializeLoadingOverlay();
        setScene(scene);
        setResizable(true);
        centerOnScreen();
        show();
        mainController.loadDataAsync(null);
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


        addButton = new Button("Add");
        addButton.addEventHandler(Event.ANY,mainController);
        addButton.setStyle("-fx-background-color: #4fc05f;-fx-background-radius: 5px;-fx-border-color: #2f7538; -fx-border-width: 1px;-fx-border-radius: 5px;-fx-text-fill: white;-fx-font-weight: bold;");
        addButton.setOnMouseEntered(e -> addButton.setStyle("-fx-background-color: #4fc05f;-fx-background-radius: 5px;-fx-border-color: #2f7538; -fx-border-width: 1px;-fx-border-radius: 5px; -fx-effect: dropshadow(gaussian, #4fc05f, 10, 0.5, 0, 0);-fx-text-fill: white;-fx-font-weight: bold;"));
        addButton.setOnMouseExited(e -> addButton.setStyle("-fx-background-color: #4fc05f;-fx-background-radius: 5px;-fx-border-color: #2f7538; -fx-border-width: 1px;-fx-border-radius: 5px;-fx-text-fill: white;-fx-font-weight: bold;"));
        addButton.setPrefSize(125, 40);
        removeButton = new Button("Remove");
        removeButton.addEventHandler(Event.ANY,mainController);
        removeButton.setStyle("-fx-background-color: #e14242;-fx-background-radius: 5px;-fx-border-color: #911010; -fx-border-width: 1px;-fx-border-radius: 5px;-fx-text-fill: white;-fx-font-weight: bold;");
        removeButton.setOnMouseEntered(e -> removeButton.setStyle("-fx-background-color: #e14242;-fx-background-radius: 5px;-fx-border-color: #911010; -fx-border-width: 1px;-fx-border-radius: 5px; -fx-effect: dropshadow(gaussian, #e14242, 10, 0.5, 0, 0);-fx-text-fill: white;-fx-font-weight: bold;"));
        removeButton.setOnMouseExited(e -> removeButton.setStyle("-fx-background-color: #e14242;-fx-background-radius: 5px;-fx-border-color: #911010; -fx-border-width: 1px;-fx-border-radius: 5px;-fx-text-fill: white;-fx-font-weight: bold;"));
        removeButton.setPrefSize(125, 40);
        changeButton = new Button("Change");
        changeButton.addEventHandler(Event.ANY,mainController);
        changeButton.setStyle("-fx-background-color: #238ac9;-fx-background-radius: 5px;-fx-border-color: #1251af; -fx-border-width: 1px;-fx-border-radius: 5px;-fx-text-fill: white;-fx-font-weight: bold;");
        changeButton.setOnMouseEntered(e -> changeButton.setStyle("-fx-background-color: #238ac9;-fx-background-radius: 5px;-fx-border-color: #1251af; -fx-border-width: 1px;-fx-border-radius: 5px; -fx-effect: dropshadow(gaussian, #238ac9, 10, 0.5, 0, 0);-fx-text-fill: white;-fx-font-weight: bold;"));
        changeButton.setOnMouseExited(e -> changeButton.setStyle("-fx-background-color: #62a0c5;-fx-background-radius: 5px;-fx-border-color: #1251af; -fx-border-width: 1px;-fx-border-radius: 5px;-fx-text-fill: white;-fx-font-weight: bold;"));
        changeButton.setPrefSize(125, 40);
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
        listViewTasks.setPrefWidth(350);
        listViewTasks.setPrefHeight(500);
        listViewTasks.setStyle("-fx-background-color: #e4ebff;-fx-border-color: #7f7fab; -fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;-fx-text-fill: white;-fx-font-size: 15px;");

        comboBox = new ComboBox<>();
        String[] types = {"Daily Habit","Weekly Habit","OneTimeTask"};
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


        nameTF.setPrefWidth(250);
        nameTF.setPrefHeight(40);
        nameTF.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        nameTF.setPromptText("Enter habit name");
        descriptionTF.setPrefHeight(40);
        descriptionTF.setPrefWidth(250);
        descriptionTF.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        descriptionTF.setPromptText("Enter habit description");
        pointsTF.setPrefHeight(40);
        pointsTF.setPrefWidth(250);
        pointsTF.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        pointsTF.setPromptText("Enter habit points");
        streakField.setPrefHeight(40);
        streakField.setPrefWidth(250);
        streakField.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        comboBox.setPrefHeight(40);
        comboBox.setPrefWidth(250);



        Label format = new Label("Habit List");
        format.setStyle("-fx-font-size: 25px;");
        listViewP.getChildren().addAll(format,listViewTasks);

        FlowPane controlButtons = new FlowPane();
        controlButtons.setHgap(10);
        controlButtons.setPadding(new Insets(10,0,0,0));

        controlButtons.getChildren().addAll(addButton,removeButton,changeButton,saveButton);

        Label head = new Label("Create Habit");
        head.setStyle("-fx-font-size: 25px;-fx-font-style: bold;");
        gridPane.add(head,0,13);

        Label l1 = new Label("Name:");
        l1.setStyle("-fx-font-size: 15px");
        gridPane.add(l1, 0, 14);
        gridPane.add(nameTF, 1, 14);

        Label l2 = new Label("Description:");
        l2.setStyle("-fx-font-size: 15px");
        gridPane.add(l2, 0, 15);
        gridPane.add(descriptionTF, 1, 15);

        Label l3 = new Label("Points:");
        l3.setStyle("-fx-font-size: 15px");
        gridPane.add(l3, 0, 16);
        gridPane.add(pointsTF, 1, 16);

        Label l4 = new Label("Type:");
        l4.setStyle("-fx-font-size: 15px");
        gridPane.add(l4, 0, 17);
        gridPane.add(comboBox, 1, 17);

        gridPane.add(controlButtons, 0, 18, 2, 1);


        nameField = new TextField();
        descriptionField = new TextField();
        pointsField = new TextField();
        typeField = new TextField();
        streakField = new TextField();
        isCompletedCB = new CheckBox();

        HBox contentWrapper = new HBox(20);
        contentWrapper.getChildren().addAll(gridPane, listViewP);


        stackPane.getChildren().setAll(contentWrapper);
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

        Label header = new Label("Dashboard");
        header.setStyle("-fx-font-size: 30px; -fx-text-fill: #f0e6ff; -fx-font-weight: bold;");

        Label subtitle = new Label("Live analytics and progression insights");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #c9b9ea;");

        HBox dashboardToolbar = new HBox(10);
        dashboardToolbar.setAlignment(Pos.CENTER_LEFT);
        Label rangeLabel = new Label("Range:");
        rangeLabel.setStyle("-fx-text-fill: #f0e6ff;");
        dashboardRangeSelector = new ComboBox<>();
        dashboardRangeSelector.getItems().setAll("Last 7 Days", "Last 30 Days", "Last Year");
        dashboardRangeSelector.getSelectionModel().selectFirst();
        dashboardRangeSelector.setStyle("-fx-background-color: #372b61; -fx-text-fill: white;");
        dashboardRangeSelector.valueProperty().addListener((obs, oldValue, newValue) -> refreshDashboardData());
        Button exportButton = new Button("Export Charts");
        exportButton.setStyle("-fx-background-color: #7c5ee6; -fx-text-fill: white; -fx-font-weight: bold;");
        exportButton.setOnAction(event -> exportDashboardAsImage());
        dashboardToolbar.getChildren().addAll(rangeLabel, dashboardRangeSelector, exportButton);

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("MM-dd");

        CategoryAxis weeklyXAxis = new CategoryAxis();
        NumberAxis weeklyYAxis = new NumberAxis();
        weeklyXAxis.setLabel("Date");
        weeklyYAxis.setLabel("Completion Rate %");
        LineChart<String, Number> weeklyCompletionChart = new LineChart<>(weeklyXAxis, weeklyYAxis);
        weeklyCompletionChart.setTitle("Weekly Completion Rate");
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
        categoryStrengthChart.setTitle("Habit Strength by Category");
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
        streakChart.setTitle("Current Streaks");
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
        xpHistoryChart.setTitle("XP History");
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
        gridPane.add(new Label("Username:"), 0, 1);
        gridPane.add(usernameTF, 1, 1);
        gridPane.add(new Label("Level:"), 0, 2);
        gridPane.add(levelTF, 1, 2);
        gridPane.add(new Label("Title:"), 0, 3);
        gridPane.add(titelTF, 1, 3);
        gridPane.add(new Label("Experience:"), 0, 4);
        gridPane.add(expirienceTF, 1, 4);
        gridPane.add(new Label("Gold:"), 0, 5);
        gridPane.add(goldTF, 1, 5);
        gridPane.add(new Label("Health:"), 0, 6);
        gridPane.add(healthTF, 1, 6);
        gridPane.add(new Label("XP Progress:"), 0, 7);
        gridPane.add(xpProgressBar, 1, 7);
        gridPane.add(new Label("HP Progress:"), 0, 8);
        gridPane.add(hpProgressBar, 1, 8);

        Label panelTitle = new Label("Player");
        panelTitle.setStyle("-fx-text-fill: #f4ecff; -fx-font-size: 26px; -fx-font-weight: bold;");
        VBox playerPanel = new VBox(12, panelTitle, gridPane);
        playerPanel.setStyle("-fx-background-color: #2d2350; -fx-background-radius: 8; -fx-padding: 16;");
        playerPanel.setPrefWidth(420);

        VBox avatarAndShop = new VBox(18, createAvatarPanel(), createShopPanel());
        avatarAndShop.setPrefWidth(520);
        statsLayout.getChildren().addAll(playerPanel, avatarAndShop);
        stackPane.getChildren().add(statsLayout);
    }

    public void refreshStatsData() {
        if (usernameTF == null || levelTF == null || titelTF == null || expirienceTF == null) {
            return;
        }
        usernameTF.setText(mainController.getUser().getUsername());
        int currentLevel = mainController.getUser().getLevel();
        levelTF.setText(String.valueOf(currentLevel));
        titelTF.setText(mainController.getUser().getTitle());
        expirienceTF.setText(String.valueOf(mainController.getUser().getExperience()));
        goldTF.setText(String.valueOf(mainController.getUser().getGold()));
        healthTF.setText(String.valueOf(mainController.getUser().getHealth()));
        int level = currentLevel;
        int xp = mainController.getUser().getExperience();
        double required = Math.max(1, level * 50.0);
        xpProgressBar.setProgress(Math.min(1.0, xp / required));
        hpProgressBar.setProgress(Math.min(1.0, mainController.getUser().getHealth() / 50.0));
        updateAvatarLayers();

        if (lastKnownLevel >= 0 && currentLevel > lastKnownLevel) {
            playLevelUpEffect();
        }
        lastKnownLevel = currentLevel;
    }

    private VBox createAvatarPanel() {
        Label title = new Label("Avatar");
        title.setStyle("-fx-text-fill: #f4ecff; -fx-font-size: 20px; -fx-font-weight: bold;");

        StackPane avatarCanvas = new StackPane();
        avatarCanvas.setPrefSize(240, 240);
        avatarCanvas.setStyle("-fx-background-color: #1d1731; -fx-background-radius: 10; -fx-border-color: #4c3d78; -fx-border-radius: 10;");

        Rectangle baseAvatar = new Rectangle(130, 170);
        baseAvatar.setFill(Color.web("#90b7ff"));
        baseAvatar.setArcHeight(20);
        baseAvatar.setArcWidth(20);

        armorLayerLabel = new Label("");
        armorLayerLabel.setStyle("-fx-text-fill: #ffd57a; -fx-font-size: 28px;");
        headgearLayerLabel = new Label("");
        headgearLayerLabel.setStyle("-fx-text-fill: #a8f3ff; -fx-font-size: 24px;");
        weaponLayerLabel = new Label("");
        weaponLayerLabel.setStyle("-fx-text-fill: #ff9ec4; -fx-font-size: 26px;");

        StackPane.setAlignment(headgearLayerLabel, Pos.TOP_CENTER);
        StackPane.setAlignment(armorLayerLabel, Pos.CENTER);
        StackPane.setAlignment(weaponLayerLabel, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(headgearLayerLabel, new Insets(32, 0, 0, 0));
        StackPane.setMargin(weaponLayerLabel, new Insets(0, 30, 18, 0));

        avatarCanvas.getChildren().addAll(baseAvatar, armorLayerLabel, headgearLayerLabel, weaponLayerLabel);
        updateAvatarLayers();

        VBox panel = new VBox(10, title, avatarCanvas);
        panel.setStyle("-fx-background-color: #2d2350; -fx-background-radius: 8; -fx-padding: 14;");
        return panel;
    }

    private VBox createShopPanel() {
        Label title = new Label("Equipment Shop");
        title.setStyle("-fx-text-fill: #f4ecff; -fx-font-size: 20px; -fx-font-weight: bold;");

        GridPane shopGrid = new GridPane();
        shopGrid.setHgap(10);
        shopGrid.setVgap(8);

        Button buySword = createShopButton("Buy Sword (15g)");
        buySword.setOnAction(e -> purchaseEquipment("weapon", "Sword", 15));
        Button buyAxe = createShopButton("Buy Axe (22g)");
        buyAxe.setOnAction(e -> purchaseEquipment("weapon", "Axe", 22));
        Button buyArmor = createShopButton("Buy Armor (20g)");
        buyArmor.setOnAction(e -> purchaseEquipment("armor", "Armor", 20));
        Button buyCape = createShopButton("Buy Cape (14g)");
        buyCape.setOnAction(e -> purchaseEquipment("armor", "Cape", 14));
        Button buyHelm = createShopButton("Buy Helm (18g)");
        buyHelm.setOnAction(e -> purchaseEquipment("headgear", "Helm", 18));
        Button buyCrown = createShopButton("Buy Crown (25g)");
        buyCrown.setOnAction(e -> purchaseEquipment("headgear", "Crown", 25));

        shopGrid.addRow(0, buySword, buyAxe);
        shopGrid.addRow(1, buyArmor, buyCape);
        shopGrid.addRow(2, buyHelm, buyCrown);

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
            mainController.getUser().spendGold(goldCost);
            if ("weapon".equals(slot)) {
                equippedWeapon = item;
            } else if ("armor".equals(slot)) {
                equippedArmor = item;
            } else if ("headgear".equals(slot)) {
                equippedHeadgear = item;
            }
            updateAvatarLayers();
            refreshStatsData();
        } catch (IllegalArgumentException exception) {
            Alert alert = new Alert(Alert.AlertType.WARNING, exception.getMessage());
            alert.setHeaderText("Shop Purchase Failed");
            alert.showAndWait();
        }
    }

    private void updateAvatarLayers() {
        if (weaponLayerLabel == null || armorLayerLabel == null || headgearLayerLabel == null) {
            return;
        }
        weaponLayerLabel.setText("None".equals(equippedWeapon) ? "" : ("Sword".equals(equippedWeapon) ? "[SWORD]" : "[AXE]"));
        armorLayerLabel.setText("None".equals(equippedArmor) ? "" : ("Armor".equals(equippedArmor) ? "[ARMOR]" : "[CAPE]"));
        headgearLayerLabel.setText("None".equals(equippedHeadgear) ? "" : ("Helm".equals(equippedHeadgear) ? "[HELM]" : "[CROWN]"));
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
        chooser.setTitle("Export Dashboard as Image");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        chooser.setInitialFileName("disciplica-dashboard.png");
        File file = chooser.showSaveDialog(this);
        if (file == null) {
            return;
        }

        try {
            WritableImage image = dashboardContainer.snapshot(new SnapshotParameters(), null);
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Chart export complete:\n" + file.getAbsolutePath());
            alert.setHeaderText("Export Successful");
            alert.showAndWait();
        } catch (IOException exception) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to export chart image: " + exception.getMessage());
            alert.setHeaderText("Export Failed");
            alert.showAndWait();
        }
    }

    public void openNewWindow() {
        Stage subStage = new Stage();
        subStage.setTitle("Info");
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
        gridPane.add(new Label("Name:"), 0, 1);
        gridPane.add(nameField, 1, 1);
        gridPane.add(new Label("Description:"), 0, 2);
        gridPane.add(descriptionField, 1, 2);
        gridPane.add(new Label("Points:"), 0, 3);
        gridPane.add(pointsField, 1, 3);
        gridPane.add(new Label("Type:"), 0, 4);
        gridPane.add(typeField, 1, 4);
        if(!info[0].equals("O")) {
            gridPane.add(new Label("Streak:"), 0, 5);
            gridPane.add(streakField, 1, 5);
            gridPane.add(new Label("Completed:"), 0, 6);
            gridPane.add(isCompletedCB, 1, 6);
        }
        else {
            gridPane.add(new Label("Completed:"), 0, 5);
            gridPane.add(isCompletedCB, 1, 5);
        }
        gridPane.add(completeButton, 0, 7, 2, 1);


        VBox vbox = new VBox();
        Scene scene = new Scene(vbox, 325, 375);

        vbox.getChildren().add(gridPane);

        subStage.setScene(scene);
        subStage.show();
    }
}
