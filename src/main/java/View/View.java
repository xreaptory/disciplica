package View;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringExpression;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Properties;
import model.domain.model.Habit;

public class View extends Stage {

    final Button dashboardBTN = new Button("Dashboard");
    final Button habitsBTN = new Button("Habits");
    final Button statsBTN = new Button("Stats");

    MainController mainController;

    final Button[] buttonsLMenu = {dashboardBTN, habitsBTN, statsBTN};

    final TextField nameTF,descriptionTF,pointsTF;

    final Button addButton,removeButton,changeButton;

    final Button addHabitButton;

    final ListView<String> listViewTasks;

    final ListView<Habit> habitListView;

    final ComboBox<String> comboBox;

    ObservableList<String> itemsObservable;

    ObservableList<Habit> habitItemsObservable;

    private final BorderPane root;
    private final VBox habitListBox;
    private final VBox statsBox;

    public ListView<String> getListViewTasks() {
        return listViewTasks;
    }

    public View() {
        mainController = new MainController(this);

        nameTF = new TextField();
        descriptionTF = new TextField();
        pointsTF = new TextField();

        addButton = new Button("Add");
        removeButton = new Button("Remove");
        changeButton = new Button("Change");

        addHabitButton = new Button("Add Habit");
        addHabitButton.setOnAction(event -> mainController.onAddHabit());

        itemsObservable = FXCollections.observableArrayList(mainController.getHabits());
        listViewTasks = new ListView<>(itemsObservable);

        habitItemsObservable = FXCollections.observableArrayList(habit -> new Observable[] { habit.completedProperty() });
        habitListView = new ListView<>(habitItemsObservable);

        comboBox = new ComboBox<>();
        String[] types = {"Daily Habit", "Weekly Habit", "OneTimeTask"};
        comboBox.getItems().addAll(types);
        comboBox.getSelectionModel().selectFirst();

        root = new BorderPane();
        root.getStyleClass().add("app-root");
        Scene scene = new Scene(root, 1100, 600);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        setTitle("Disciplica");
        if (Properties.applicationImageIconAsICO != null) {
            getIcons().add(Properties.applicationImageIconAsICO);
        }
        MenuBar menuBar = new MenuBar();
        menuBar.getStyleClass().add("app-menu");
        menuBar.getMenus().addAll(new Menu("File"), new Menu("Edit"), new Menu("View"));
        root.setTop(menuBar);

        VBox leftMenu = new VBox(10);
        leftMenu.getStyleClass().add("nav-panel");
        leftMenu.setPadding(new Insets(12, 12, 12, 12));
        leftMenu.setPrefWidth(180);
        for (Button b : buttonsLMenu) {
            b.setMaxWidth(Double.MAX_VALUE);
            b.getStyleClass().add("nav-button");
        }
        dashboardBTN.setOnAction(event -> showHabitsView());
        habitsBTN.setOnAction(event -> showHabitsView());
        statsBTN.setOnAction(event -> showStatsView());
        leftMenu.getChildren().addAll(dashboardBTN, habitsBTN, statsBTN);
        root.setLeft(leftMenu);

        habitListBox = new VBox(10);
        habitListBox.getStyleClass().add("center-panel");
        habitListBox.setPadding(new Insets(12, 12, 12, 12));
        Label habitListTitle = new Label("Habit List");
        habitListTitle.getStyleClass().add("section-title");
        addHabitButton.getStyleClass().add("action-button");
        habitListView.getStyleClass().add("habit-list");
        StringExpression levelText = Bindings.format("Level: %d (%d XP)",
                mainController.levelProperty(),
                mainController.experienceProperty());
        Label levelLabel = new Label();
        levelLabel.textProperty().bind(levelText);

        DoubleBinding completionPercentBinding = Bindings.createDoubleBinding(() -> {
            int total = habitItemsObservable.size();
            if (total == 0) return 0.0;
            long completed = habitItemsObservable.stream().filter(Habit::isCompleted).count();
            return (double) completed / total;
        }, habitItemsObservable);
        ProgressBar completionProgress = new ProgressBar(0);
        completionProgress.progressProperty().bind(completionPercentBinding);
        HBox progressRow = new HBox(10, levelLabel, completionProgress);
        HBox.setHgrow(completionProgress, Priority.ALWAYS);
        habitListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Habit item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("habit-cell", "habit-cell-complete", "habit-cell-incomplete");
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item.getName() + " - " + item.getDescription());
                getStyleClass().add("habit-cell");
                if (item.isCompleted()) {
                    getStyleClass().add("habit-cell-complete");
                } else {
                    getStyleClass().add("habit-cell-incomplete");
                }
            }
        });
        habitListBox.getChildren().addAll(habitListTitle, progressRow, addHabitButton, habitListView);
        VBox.setVgrow(habitListView, Priority.ALWAYS);
        statsBox = buildStatsView();
        showHabitsView();

        HBox statusBar = new HBox(12);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setPadding(new Insets(8, 12, 8, 12));
        Label statusLabel = new Label("Ready");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label lastSavedLabel = new Label("Last saved: ...");
        statusBar.getChildren().addAll(statusLabel, spacer, lastSavedLabel);
        root.setBottom(statusBar);

        setScene(scene);
        setResizable(true);
        centerOnScreen();
        show();
    }

    private void showHabitsView() {
        root.setCenter(habitListBox);
    }

    private void showStatsView() {
        root.setCenter(statsBox);
    }

    private VBox buildStatsView() {
        VBox container = new VBox(16);
        container.getStyleClass().add("center-panel");
        container.setPadding(new Insets(12, 12, 12, 12));

        Label title = new Label("Statistics");
        title.getStyleClass().add("section-title");

        BarChart<String, Number> weeklyChart = buildWeeklyCompletionChart();
        weeklyChart.getStyleClass().add("stats-chart");

        PieChart categoryChart = buildCategoryDistributionChart();
        categoryChart.getStyleClass().add("stats-chart");

        LineChart<Number, Number> xpChart = buildXpGrowthChart();
        xpChart.getStyleClass().add("stats-chart");

        HBox topRow = new HBox(16, weeklyChart, categoryChart);
        HBox.setHgrow(weeklyChart, Priority.ALWAYS);
        HBox.setHgrow(categoryChart, Priority.ALWAYS);

        VBox.setVgrow(weeklyChart, Priority.ALWAYS);
        VBox.setVgrow(categoryChart, Priority.ALWAYS);
        VBox.setVgrow(xpChart, Priority.ALWAYS);

        container.getChildren().addAll(title, topRow, xpChart);
        return container;
    }

    private BarChart<String, Number> buildWeeklyCompletionChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Habits Completed (This Week)");
        xAxis.setLabel("Day");
        yAxis.setLabel("Completed");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Completed");
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        int[] values = {2, 4, 3, 5, 1, 6, 4};
        for (int i = 0; i < days.length; i++) {
            series.getData().add(new XYChart.Data<>(days[i], values[i]));
        }
        chart.getData().add(series);
        chart.setAnimated(false);
        return chart;
    }

    private PieChart buildCategoryDistributionChart() {
        PieChart chart = new PieChart();
        chart.setTitle("Habit Categories");
        chart.getData().addAll(
                new PieChart.Data("Health", 5),
                new PieChart.Data("Learning", 3),
                new PieChart.Data("Productivity", 4),
                new PieChart.Data("Mindfulness", 2)
        );
        chart.setLabelsVisible(true);
        return chart;
    }

    private LineChart<Number, Number> buildXpGrowthChart() {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("XP Growth");
        xAxis.setLabel("Day");
        yAxis.setLabel("XP");

        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("XP");
        int[] xp = {200, 350, 520, 680, 860, 1020, 1200};
        for (int i = 0; i < xp.length; i++) {
            series.getData().add(new XYChart.Data<>(i + 1, xp[i]));
        }
        chart.getData().add(series);
        chart.setAnimated(false);
        chart.setCreateSymbols(true);
        return chart;
    }

    public void openNewWindow() {

        mainController = new MainController(this);

        Stage subStage = new Stage();
        subStage.setTitle("Info");

        String[] info = mainController.getInfo();

        TextField nameField = new TextField();
        nameField.setEditable(false);
        nameField.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        TextField descriptionField = new TextField();
        descriptionField.setEditable(false);
        descriptionField.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        TextField pointsField = new TextField();
        pointsField.setEditable(false);
        pointsField.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        TextField typeField = new TextField();
        typeField.setEditable(false);
        typeField.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        TextField streakField = new TextField();
        streakField.setEditable(false);
        streakField.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        TextField isCompletedField = new TextField();
        isCompletedField.setEditable(false);
        isCompletedField.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");

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
            isCompletedField.setText(info[4]);
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
            isCompletedField.setText(info[4]);
        }

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
            gridPane.add(isCompletedField, 1, 6);
        }
        else {
            gridPane.add(new Label("Completed:"), 0, 5);
            gridPane.add(isCompletedField, 1, 5);
        }


        VBox vbox = new VBox();
        Scene scene = new Scene(vbox, 325, 350);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        vbox.getStyleClass().add("app-root");

        vbox.getChildren().add(gridPane);

        subStage.setScene(scene);
        subStage.show();
    }
}
