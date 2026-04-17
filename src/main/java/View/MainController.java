package View;

import com.disciplica.domain.model.Habit;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import model.Properties;
import model.SCTools;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class MainController {

    @FXML
    private MenuItem menuNewAppMI;

    @FXML
    private MenuItem menuCloseMI;

    @FXML
    private Button dashboardBTN;

    @FXML
    private Button habitsBTN;

    @FXML
    private Button statsBTN;

    @FXML
    private Label statusLabel;

    @FXML
    private Label lastSavedLabel;

    @FXML
    private ListView<Habit> habitListView;

    private final ObservableList<Habit> habits = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        String formattedNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        lastSavedLabel.setText("Last saved: " + formattedNow);
        habitListView.setItems(habits);
        habitListView.setPlaceholder(new Label("No habits yet."));
        registerHoverStyles(dashboardBTN, habitsBTN, statsBTN);
        habitListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Habit habit, boolean empty) {
                super.updateItem(habit, empty);
                if (empty || habit == null) {
                    setText(null);
                    return;
                }
                setText(habit.getName() + " - " + habit.getDescription());
            }
        });
    }

    private void registerHoverStyles(Button... buttons) {
        for (Button button : buttons) {
            button.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
                button.getScene().getRoot().setCursor(javafx.scene.Cursor.HAND);
                SCTools.BorderToNode(button, Properties.FocusOnComponentColor, 10);
            });

            button.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
                button.getScene().getRoot().setCursor(javafx.scene.Cursor.DEFAULT);
                button.setEffect(null);
            });
        }
    }

    @FXML
    private void onAddHabit() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Habit");
        dialog.setHeaderText("Enter habit details");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Habit name");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Habit description");
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(4);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descriptionArea, 1, 1);
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(descriptionArea, Priority.ALWAYS);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != addButtonType) {
            return;
        }

        try {
            Habit newHabit = new Habit(nameField.getText(), descriptionArea.getText());
            habits.add(newHabit);
            statusLabel.setText("Habit added: " + newHabit.getName());
        } catch (IllegalArgumentException exception) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Cannot add habit");
            errorAlert.setContentText(exception.getMessage());
            errorAlert.showAndWait();
            statusLabel.setText("Failed to add habit");
        }
    }

    @FXML
    private void handleNewWindow() {
        new View();
    }

    @FXML
    private void handleClose() {
        MyAlertFX alert = new MyAlertFX(
                dashboardBTN.getScene().getWindow(),
                Alert.AlertType.CONFIRMATION,
                "Close application",
                "Exit Disciplica",
                "Do you really want to close?",
                true,
                Properties.applicationImageIconAsICO,
                "OK",
                "Cancel",
                Properties.ButtonBackgroundColor,
                Color.DARKRED,
                Properties.FocusOnComponentColor
        );

        ButtonType result = alert.getResult().orElse(ButtonType.CANCEL);
        if (result.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            Platform.exit();
        }
    }

    @FXML
    private void handleDashboard() {
        statusLabel.setText("Ready - Dashboard selected");
    }

    @FXML
    private void handleHabits() {
        statusLabel.setText("Ready - Habits selected");
    }

    @FXML
    private void handleStats() {
        statusLabel.setText("Ready - Stats selected");
    }
}
