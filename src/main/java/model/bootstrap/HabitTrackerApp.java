package model.bootstrap;

import View.View;
import javafx.application.Application;
import javafx.stage.Stage;

public class HabitTrackerApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        new View();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
