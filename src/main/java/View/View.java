package View;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.*;
import model.Properties;
import java.io.IOException;

public class View extends Stage {

    public View() {
        Scene scene = loadSceneFromFxml();
        setTitle("Habit Tracker - MVC");
        if (Properties.applicationImageIconAsICO != null) {
            getIcons().add(Properties.applicationImageIconAsICO);
        }
        setScene(scene);
        setResizable(true);
        centerOnScreen();
        show();
    }

    private Scene loadSceneFromFxml() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/main-view.fxml"));
            return new Scene(root, 900, 600);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load /fxml/main-view.fxml", exception);
        }
    }

}
