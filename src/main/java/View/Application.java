package View;

import javafx.application.Application;
import javafx.stage.Stage;


public class Application extends Application{

    @Override
    public void start(Stage stage) throws Exception {
        new View();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
