package View;

import com.disciplica.domain.model.Habit;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.*;
import model.Properties;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
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





public class View extends Stage {

    final Button dashboardBTN = new Button("Dashboard");
    final Button habitsBTN = new Button("Habits");
    final Button statsBTN = new Button("Stats");


    public View() {
        HBox hbox = new HBox();
        Scene scene = new Scene(hbox, 1000, 600);
        setTitle("Disciplica");
        if (Properties.applicationImageIconAsICO != null) {
            getIcons().add(Properties.applicationImageIconAsICO);
        }

        FlowPane leftMenu = new FlowPane();

        leftMenu.setPadding(new Insets(200,50,0,50));
        leftMenu.setStyle("-fx-background-color: #bbbbbb");
        leftMenu.setVgap(25);
        leftMenu.setOrientation(Orientation.VERTICAL);
        Button[] buttonsLMenu = {dashboardBTN, habitsBTN, statsBTN};
        for (Button b : buttonsLMenu) {
            b.setPrefSize(110, 35);
            b.setStyle("-fx-background-color: #e3d6af;-fx-background-radius: 5px;-fx-border-color: #b2a56a; -fx-border-width: 2px;-fx-border-radius: 5px;");
            b.setOnMouseEntered(e -> b.setStyle("-fx-background-color: #e3d6af;-fx-background-radius: 5px;-fx-border-color: #b2a56a; -fx-border-width: 2px;-fx-border-radius: 5px; -fx-effect: dropshadow(gaussian, #b2a56a, 10, 0.5, 0, 0);"));
            b.setOnMouseExited(e -> b.setStyle("-fx-background-color: #e3d6af;-fx-background-radius: 5px;-fx-border-color: #b2a56a; -fx-border-width: 2px;-fx-border-radius: 5px;"));
        }
        leftMenu.getChildren().addAll(dashboardBTN, habitsBTN, statsBTN);

        hbox.getChildren().add(leftMenu);

        GridPane rightMenu = new GridPane();



        hbox.getChildren().add(rightMenu);
        setScene(scene);
        setResizable(true);
        centerOnScreen();
        show();
    }
}
