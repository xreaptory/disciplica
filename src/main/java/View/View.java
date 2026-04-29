package View;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Properties;

public class View extends Stage {

    final Button dashboardBTN = new Button("Dashboard");
    final Button habitsBTN = new Button("Habits");
    final Button statsBTN = new Button("Stats");

    final MainController mainController;

    final Button[] buttonsLMenu = {dashboardBTN, habitsBTN, statsBTN};

    final TextField nameTF,descriptionTF,pointsTF;

    final Button addButton,removeButton,changeButton;

    final ListView<String> listViewTasks;

    final ComboBox<String> comboBox;

    ObservableList<String> itemsObservable;

    public View() {

        mainController = new MainController(this);

        HBox hbox = new HBox();
        Scene scene = new Scene(hbox, 1100, 600);
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
        addButton.setStyle("-fx-background-color: #62a0c5;-fx-background-radius: 5px;-fx-border-color: #204170; -fx-border-width: 1px;-fx-border-radius: 5px;");
        addButton.setOnMouseEntered(e -> addButton.setStyle("-fx-background-color: #62a0c5;-fx-background-radius: 5px;-fx-border-color: #204170; -fx-border-width: 1px;-fx-border-radius: 5px; -fx-effect: dropshadow(gaussian, #4c8bbf, 10, 0.5, 0, 0);"));
        addButton.setOnMouseExited(e -> addButton.setStyle("-fx-background-color: #62a0c5;-fx-background-radius: 5px;-fx-border-color: #204170; -fx-border-width: 1px;-fx-border-radius: 5px;"));
        addButton.setPrefSize(90, 30);
        removeButton = new Button("Remove");
        removeButton.addEventHandler(Event.ANY,mainController);
        removeButton.setStyle("-fx-background-color: #62a0c5;-fx-background-radius: 5px;-fx-border-color: #204170; -fx-border-width: 1px;-fx-border-radius: 5px;");
        removeButton.setOnMouseEntered(e -> removeButton.setStyle("-fx-background-color: #62a0c5;-fx-background-radius: 5px;-fx-border-color: #204170; -fx-border-width: 1px;-fx-border-radius: 5px; -fx-effect: dropshadow(gaussian, #4c8bbf, 10, 0.5, 0, 0);"));
        removeButton.setOnMouseExited(e -> removeButton.setStyle("-fx-background-color: #62a0c5;-fx-background-radius: 5px;-fx-border-color: #204170; -fx-border-width: 1px;-fx-border-radius: 5px;"));
        removeButton.setPrefSize(90, 30);
        changeButton = new Button("Change");
        changeButton.addEventHandler(Event.ANY,mainController);
        changeButton.setStyle("-fx-background-color: #62a0c5;-fx-background-radius: 5px;-fx-border-color: #204170; -fx-border-width: 1px;-fx-border-radius: 5px;");
        changeButton.setOnMouseEntered(e -> changeButton.setStyle("-fx-background-color: #62a0c5;-fx-background-radius: 5px;-fx-border-color: #204170; -fx-border-width: 1px;-fx-border-radius: 5px; -fx-effect: dropshadow(gaussian, #4c8bbf, 10, 0.5, 0, 0);"));
        changeButton.setOnMouseExited(e -> changeButton.setStyle("-fx-background-color: #62a0c5;-fx-background-radius: 5px;-fx-border-color: #204170; -fx-border-width: 1px;-fx-border-radius: 5px;"));
        changeButton.setPrefSize(90, 30);

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
        comboBox.setStyle("-fx-background-color: #62a0c5;-fx-background-radius: 5px;-fx-border-color: #204170; -fx-border-width: 1px;-fx-border-radius: 5px;");
        comboBox.getSelectionModel().selectFirst();

        nameTF.setMaxWidth(200);
        descriptionTF.setMaxWidth(200);
        pointsTF.setMaxWidth(200);
        comboBox.setMaxWidth(200);

        Label format = new Label("Habits Format: name;description;points;isComplete;streak");
        format.setStyle("-fx-font-size: 15px;");
        listViewP.getChildren().addAll(format,listViewTasks);

        FlowPane controlButtons = new FlowPane();
        controlButtons.setHgap(10);

        controlButtons.getChildren().addAll(addButton,removeButton,changeButton);

        Label head = new Label("Create Habit");
        head.setStyle("-fx-font-size: 22px;-fx-font-style: bold;");
        gridPane.add(head,0,0);

        Label l1 = new Label("Name:");
        l1.setStyle("-fx-font-size: 15px");
        gridPane.add(l1, 0, 1);
        gridPane.add(nameTF, 1, 1);

        Label l2 = new Label("Description:");
        l2.setStyle("-fx-font-size: 15px");
        gridPane.add(l2, 0, 2);
        gridPane.add(descriptionTF, 1, 2);

        Label l3 = new Label("Points:");
        l3.setStyle("-fx-font-size: 15px");
        gridPane.add(l3, 0, 3);
        gridPane.add(pointsTF, 1, 3);

        Label l4 = new Label("Type:");
        l4.setStyle("-fx-font-size: 15px");
        gridPane.add(l4, 0, 4);
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
