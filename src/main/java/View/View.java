package View;

import com.google.inject.Injector;
import javafx.application.Platform;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Properties;
import model.domain.model.AbstractTask;
import model.domain.model.DailyHabit;
import model.domain.model.WeeklyHabit;
import model.service.UserService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

    public Button addButton,removeButton,changeButton;

    public ListView<String> listViewTasks;

    public ComboBox<String> comboBox;

    final Button completeButton = new Button("Complete");
    final Button saveButton = new Button("Save");
    private Timeline dashboardRefreshTimeline;
    private XYChart.Series<String, Number> dashboardCompletionSeries;
    private PieChart dashboardCategoryChart;
    private XYChart.Series<String, Number> dashboardXpSeries;

    ObservableList<String> itemsObservable;
    private StackPane loadingOverlay;
    private Label loadingLabel;

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
        VBox container = new VBox(16);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #241a3a;");

        Label header = new Label("Dashboard");
        header.setStyle("-fx-font-size: 30px; -fx-text-fill: #f0e6ff; -fx-font-weight: bold;");

        Label subtitle = new Label("Live progress and habit insights");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #c9b9ea;");

        CategoryAxis completionXAxis = new CategoryAxis();
        NumberAxis completionYAxis = new NumberAxis();
        completionXAxis.setLabel("Day");
        completionYAxis.setLabel("Completed");
        BarChart<String, Number> completionsChart = new BarChart<>(completionXAxis, completionYAxis);
        completionsChart.setTitle("Habits Completed This Week");
        completionsChart.setLegendVisible(false);
        completionsChart.setAnimated(false);

        XYChart.Series<String, Number> completionSeries = new XYChart.Series<>();
        Map<String, Integer> liveCompletionData = createLiveCompletionData();
        liveCompletionData.forEach((day, value) -> completionSeries.getData().add(new XYChart.Data<>(day, value)));
        completionsChart.getData().add(completionSeries);

        PieChart categoryChart = new PieChart();
        categoryChart.setTitle("Habit Category Distribution");
        categoryChart.setLabelsVisible(true);
        for (PieChart.Data data : createLiveCategoryData()) {
            categoryChart.getData().add(data);
        }

        CategoryAxis xpXAxis = new CategoryAxis();
        NumberAxis xpYAxis = new NumberAxis();
        xpXAxis.setLabel("Time");
        xpYAxis.setLabel("XP");
        LineChart<String, Number> xpChart = new LineChart<>(xpXAxis, xpYAxis);
        xpChart.setTitle("XP Growth (Live)");
        xpChart.setCreateSymbols(true);
        xpChart.setAnimated(false);
        xpChart.setLegendVisible(false);

        XYChart.Series<String, Number> xpSeries = new XYChart.Series<>();
        seedLiveXpSeries(xpSeries);
        xpChart.getData().add(xpSeries);

        dashboardCompletionSeries = completionSeries;
        dashboardCategoryChart = categoryChart;
        dashboardXpSeries = xpSeries;

        GridPane chartGrid = new GridPane();
        chartGrid.setHgap(16);
        chartGrid.setVgap(16);
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(50);
        chartGrid.getColumnConstraints().addAll(col, col);
        RowConstraints row = new RowConstraints();
        row.setVgrow(Priority.ALWAYS);
        chartGrid.getRowConstraints().addAll(row, row);

        styleHabiticaChart(completionsChart);
        styleHabiticaChart(categoryChart);
        styleHabiticaChart(xpChart);
        applyHabiticaSeriesColors(completionsChart, xpChart, categoryChart);

        completionsChart.setPrefHeight(300);
        categoryChart.setPrefHeight(300);
        xpChart.setPrefHeight(280);
        GridPane.setColumnSpan(xpChart, 2);

        chartGrid.add(completionsChart, 0, 0);
        chartGrid.add(categoryChart, 1, 0);
        chartGrid.add(xpChart, 0, 1);

        VBox.setVgrow(chartGrid, Priority.ALWAYS);
        container.getChildren().addAll(header, subtitle, chartGrid);
        container.setAlignment(Pos.TOP_LEFT);
        stackPane.getChildren().add(container);

        startLiveDashboardRefresh(completionSeries, categoryChart, xpSeries);
    }

    public void openStats(){
        stackPane.getChildren().clear();
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

        stackPane.getChildren().add(gridPane);
    }

    public void refreshStatsData() {
        if (usernameTF == null || levelTF == null || titelTF == null || expirienceTF == null) {
            return;
        }
        usernameTF.setText(mainController.getUser().getUsername());
        levelTF.setText(String.valueOf(mainController.getUser().getLevel()));
        titelTF.setText(mainController.getUser().getTitle());
        expirienceTF.setText(String.valueOf(mainController.getUser().getExperience()));
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

    private void applyHabiticaSeriesColors(BarChart<String, Number> barChart,
                                           LineChart<String, Number> lineChart,
                                           PieChart pieChart) {
        Platform.runLater(() -> {
            for (Node bar : barChart.lookupAll(".default-color0.chart-bar")) {
                bar.setStyle("-fx-bar-fill: #f5c451;");
            }

            Node line = lineChart.lookup(".default-color0.chart-series-line");
            if (line != null) {
                line.setStyle("-fx-stroke: #6bd7ff; -fx-stroke-width: 2.2px;");
            }
            for (Node symbol : lineChart.lookupAll(".default-color0.chart-line-symbol")) {
                symbol.setStyle("-fx-background-color: #6bd7ff, #241a3a;");
            }

            String[] pieColors = {"#8e6bff", "#6bd7ff", "#f5c451", "#ff8a65"};
            for (int i = 0; i < pieChart.getData().size(); i++) {
                Node slice = pieChart.getData().get(i).getNode();
                if (slice != null) {
                    String color = pieColors[i % pieColors.length];
                    slice.setStyle("-fx-pie-color: " + color + ";");
                }
            }
        });
    }

    private Map<String, Integer> createLiveCompletionData() {
        Map<String, Integer> data = new LinkedHashMap<>();
        for (int daysAgo = 6; daysAgo >= 0; daysAgo--) {
            LocalDate day = LocalDate.now().minusDays(daysAgo);
            String dayLabel = day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            data.put(dayLabel, mainController.getUser().getCompletionCountForDate(day));
        }
        return data;
    }

    private ObservableList<PieChart.Data> createLiveCategoryData() {
        int daily = 0;
        int weekly = 0;
        int oneTime = 0;
        for (AbstractTask task : mainController.getUser().getTasks()) {
            if (task instanceof DailyHabit) {
                daily++;
            } else if (task instanceof WeeklyHabit) {
                weekly++;
            } else {
                oneTime++;
            }
        }
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        pieData.add(new PieChart.Data("Daily", daily));
        pieData.add(new PieChart.Data("Weekly", weekly));
        pieData.add(new PieChart.Data("One-Time", oneTime));
        return pieData;
    }

    private void seedLiveXpSeries(XYChart.Series<String, Number> xpSeries) {
        List<Integer> xpWindow = mainController.getUser().getXpHistoryWindow(7);
        for (int i = 0; i < xpWindow.size(); i++) {
            String label = i == xpWindow.size() - 1 ? "Now" : String.valueOf(i);
            xpSeries.getData().add(new XYChart.Data<>(label, xpWindow.get(i)));
        }
    }

    private void startLiveDashboardRefresh(XYChart.Series<String, Number> completionSeries,
                                           PieChart categoryChart,
                                           XYChart.Series<String, Number> xpSeries) {
        if (dashboardRefreshTimeline != null) {
            dashboardRefreshTimeline.stop();
        }

        dashboardRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event ->
                refreshDashboardDataInternal(completionSeries, categoryChart, xpSeries)));
        dashboardRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        dashboardRefreshTimeline.play();
    }

    public void refreshDashboardData() {
        if (dashboardCompletionSeries == null || dashboardCategoryChart == null || dashboardXpSeries == null) {
            refreshStatsData();
            return;
        }
        refreshDashboardDataInternal(dashboardCompletionSeries, dashboardCategoryChart, dashboardXpSeries);
        refreshStatsData();
    }

    private void refreshDashboardDataInternal(XYChart.Series<String, Number> completionSeries,
                                              PieChart categoryChart,
                                              XYChart.Series<String, Number> xpSeries) {
        Map<String, Integer> completionData = createLiveCompletionData();
        int i = 0;
        for (Map.Entry<String, Integer> entry : completionData.entrySet()) {
            completionSeries.getData().get(i).setXValue(entry.getKey());
            completionSeries.getData().get(i).setYValue(entry.getValue());
            i++;
        }

        categoryChart.setData(createLiveCategoryData());

        List<Integer> xpWindow = mainController.getUser().getXpHistoryWindow(7);
        xpSeries.getData().clear();
        for (int index = 0; index < xpWindow.size(); index++) {
            String label = index == xpWindow.size() - 1 ? "Now" : String.valueOf(index);
            xpSeries.getData().add(new XYChart.Data<>(label, xpWindow.get(index)));
        }

        applyHabiticaSeriesColors((BarChart<String, Number>) completionSeries.getChart(),
                (LineChart<String, Number>) xpSeries.getChart(),
                categoryChart);
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
