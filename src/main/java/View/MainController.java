package View;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;

public class MainController {
    // Optional fields bound from FXML (keeps controller consistent with FXML ids)
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
    private ListView<?> habitListView;

    @FXML
    private Label statusLabel;

    @FXML
    private Label lastSavedLabel;

    // Event handler methods referenced by main-view.fxml
    @FXML
    private void handleNewWindow(ActionEvent event) {
        System.out.println("handleNewWindow invoked");
        // Minimal stub: real implementation can open a new Stage
    }

    @FXML
    private void handleClose(ActionEvent event) {
        System.out.println("handleClose invoked");
        // Minimal stub: real implementation can close the window
    }

    @FXML
    private void handleDashboard(ActionEvent event) {
        System.out.println("handleDashboard invoked");
    }

    @FXML
    private void handleHabits(ActionEvent event) {
        System.out.println("handleHabits invoked");
    }

    @FXML
    private void handleStats(ActionEvent event) {
        System.out.println("handleStats invoked");
    }

    @FXML
    private void onAddHabit(ActionEvent event) {
        System.out.println("onAddHabit invoked");
    }
}
