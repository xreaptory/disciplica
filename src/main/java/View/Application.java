package View;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.stage.Stage;
import model.di.AppModule;


public class Application extends javafx.application.Application {
    private final Injector injector = Guice.createInjector(new AppModule());

    @Override
    public void start(Stage stage) throws Exception {
        new View(injector);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
