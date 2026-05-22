package model.bootstrap;

import View.View;
import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.stage.Stage;
import model.di.AppModule;

public class HabitTrackerApp extends Application {
    private final Injector injector = Guice.createInjector(new AppModule());

    @Override
    public void start(Stage primaryStage) {
        new View(injector);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
