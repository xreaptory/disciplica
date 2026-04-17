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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class MainController {


    @FXML
    private void handleNewWindow() {
        new View();
    }

    @FXML
    private void handleClose() {
        Alert alert = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Do you really want to close?",
                new ButtonType("OK", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE)
        );

        ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);
        if (result.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            Platform.exit();
        }
    }

}
