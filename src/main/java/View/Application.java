package View;

import javafx.stage.Stage;


public class Application extends javafx.application.Application {

    @Override
    public void start(Stage stage) throws Exception {
        new View();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
