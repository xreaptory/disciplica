package View;

import com.disciplica.domain.model.AbstractTask;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Properties;

public class View extends Stage {

    final Button dashboardBTN = new Button("Dashboard");
    final Button habitsBTN = new Button("Habits");
    final Button statsBTN = new Button("Stats");

    final MainController mainController;

    final Button[] buttonsLMenu = {dashboardBTN, habitsBTN, statsBTN};

    final TextField nameTF,descriptionTF,pointsTF;

    final Button addButton,removeButton;

    final ListView<String> listViewTasks;

    final ComboBox<String> comboBox;

    ObservableList<String> itemsObservable;

    public View() {

        mainController = new MainController(this);

        HBox hbox = new HBox();
        Scene scene = new Scene(hbox, 1000, 600);
        setTitle("Disciplica");
        if (Properties.applicationImageIconAsICO != null) {
            getIcons().add(Properties.applicationImageIconAsICO);
        }

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25, 25, 25, 25));

        FlowPane leftMenu = new FlowPane();

        leftMenu.setPadding(new Insets(200,50,0,50));
        leftMenu.setStyle("-fx-background-color: #bbbbbb");
        leftMenu.setVgap(25);
        leftMenu.setOrientation(Orientation.VERTICAL);
        for (Button b : buttonsLMenu) {
            b.setPrefSize(110, 35);
            b.setStyle("-fx-background-color: #62a0c5;-fx-background-radius: 5px;-fx-border-color: #204170; -fx-border-width: 1px;-fx-border-radius: 5px;");
            b.setOnMouseEntered(e -> b.setStyle("-fx-background-color: #62a0c5;-fx-background-radius: 5px;-fx-border-color: #204170; -fx-border-width: 1px;-fx-border-radius: 5px; -fx-effect: dropshadow(gaussian, #4c8bbf, 10, 0.5, 0, 0);"));
            b.setOnMouseExited(e -> b.setStyle("-fx-background-color: #62a0c5;-fx-background-radius: 5px;-fx-border-color: #204170; -fx-border-width: 1px;-fx-border-radius: 5px;"));
            b.addEventHandler(Event.ANY,mainController);
        }
        leftMenu.getChildren().addAll(dashboardBTN, habitsBTN, statsBTN);

        nameTF = new TextField();
        nameTF.addEventHandler(Event.ANY,mainController);
        descriptionTF = new TextField();
        descriptionTF.addEventHandler(Event.ANY,mainController);
        pointsTF = new TextField();
        pointsTF.addEventHandler(Event.ANY,mainController);


        addButton = new Button("Add");
        addButton.addEventHandler(Event.ANY,mainController);
        addButton.setPrefSize(110, 35);
        removeButton = new Button("Remove");
        removeButton.addEventHandler(Event.ANY,mainController);
        removeButton.setPrefSize(110, 35);

        FlowPane listViewP = new FlowPane();
        listViewP.setOrientation(Orientation.VERTICAL);

        itemsObservable = FXCollections.observableArrayList(mainController.getHabits());
        listViewTasks = new ListView<>(itemsObservable);
        listViewTasks.getSelectionModel().selectedItemProperty().addListener(mainController);
        listViewTasks.getSelectionModel().selectFirst();
        listViewTasks.addEventHandler(Event.ANY,mainController);
        listViewTasks.setPrefWidth(300);

        comboBox = new ComboBox<>();
        String[] types = {"Daily Habit","Weekly Habit","OneTimeTask"};
        comboBox.getItems().addAll(types);
        comboBox.getSelectionModel().selectFirst();

        nameTF.setMaxWidth(200);
        descriptionTF.setMaxWidth(200);
        pointsTF.setMaxWidth(200);
        comboBox.setMaxWidth(200);

        listViewP.getChildren().addAll(new Label("Habits Format: name;description;points;isComplete;streak"),listViewTasks);

        FlowPane controlButtons = new FlowPane();
        controlButtons.setHgap(10);

        controlButtons.getChildren().addAll(addButton,removeButton);

        gridPane.add(new Label("Create Habit"),0,0);

        gridPane.add(new Label("Name:"), 0, 1);
        gridPane.add(nameTF, 1, 1);

        gridPane.add(new Label("Description:"), 0, 2);
        gridPane.add(descriptionTF, 1, 2);

        gridPane.add(new Label("Points:"), 0, 3);
        gridPane.add(pointsTF, 1, 3);

        gridPane.add(new Label("Type:"), 0, 4);
        gridPane.add(comboBox, 1, 4);

        gridPane.add(controlButtons, 0, 5, 2, 1);


        hbox.getChildren().add(leftMenu);
        hbox.getChildren().add(gridPane);
        hbox.getChildren().add(listViewP);

        GridPane rightMenu = new GridPane();



        hbox.getChildren().add(rightMenu);
        setScene(scene);
        setResizable(true);
        centerOnScreen();
        show();
    }
}
