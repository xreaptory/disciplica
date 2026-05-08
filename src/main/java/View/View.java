package View;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import model.Properties;

public class View extends Stage {

    final Button dashboardBTN = new Button("Dashboard");
    final Button habitsBTN = new Button("Habits");
    final Button statsBTN = new Button("Stats");

    MainController mainController;

    final Button[] buttonsLMenu = {dashboardBTN, habitsBTN, statsBTN};

    final TextField nameTF,descriptionTF,pointsTF;

    final Button addButton,removeButton,changeButton;

    final ListView<String> listViewTasks;

    final ComboBox<String> comboBox;

    ObservableList<String> itemsObservable;

    public ListView<String> getListViewTasks() {
        return listViewTasks;
    }

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
            b.setPrefSize(150, 45);
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
        addButton.setStyle("-fx-background-color: #4fc05f;-fx-background-radius: 5px;-fx-border-color: #2f7538; -fx-border-width: 1px;-fx-border-radius: 5px;-fx-text-fill: white;-fx-font-weight: bold;");
        addButton.setOnMouseEntered(e -> addButton.setStyle("-fx-background-color: #4fc05f;-fx-background-radius: 5px;-fx-border-color: #2f7538; -fx-border-width: 1px;-fx-border-radius: 5px; -fx-effect: dropshadow(gaussian, #4fc05f, 10, 0.5, 0, 0);-fx-text-fill: white;-fx-font-weight: bold;"));
        addButton.setOnMouseExited(e -> addButton.setStyle("-fx-background-color: #4fc05f;-fx-background-radius: 5px;-fx-border-color: #2f7538; -fx-border-width: 1px;-fx-border-radius: 5px;-fx-text-fill: white;-fx-font-weight: bold;"));
        addButton.setPrefSize(125, 40);
        removeButton = new Button("Remove");
        removeButton.addEventHandler(Event.ANY,mainController);
        removeButton.setStyle("-fx-background-color: #e14242;-fx-background-radius: 5px;-fx-border-color: #911010; -fx-border-width: 1px;-fx-border-radius: 5px;-fx-text-fill: white;-fx-font-weight: bold;");
        removeButton.setOnMouseEntered(e -> removeButton.setStyle("-fx-background-color: #e14242;-fx-background-radius: 5px;-fx-border-color: #911010; -fx-border-width: 1px;-fx-border-radius: 5px; -fx-effect: dropshadow(gaussian, #e14242, 10, 0.5, 0, 0);-fx-text-fill: white;-fx-font-weight: bold;"));
        removeButton.setOnMouseExited(e -> removeButton.setStyle("-fx-background-color: #e14242;-fx-background-radius: 5px;-fx-border-color: #911010; -fx-border-width: 1px;-fx-border-radius: 5px;-fx-text-fill: white;-fx-font-weight: bold;"));
        removeButton.setPrefSize(125, 40);
        changeButton = new Button("Change");
        changeButton.addEventHandler(Event.ANY,mainController);
        changeButton.setStyle("-fx-background-color: #238ac9;-fx-background-radius: 5px;-fx-border-color: #1251af; -fx-border-width: 1px;-fx-border-radius: 5px;-fx-text-fill: white;-fx-font-weight: bold;");
        changeButton.setOnMouseEntered(e -> changeButton.setStyle("-fx-background-color: #238ac9;-fx-background-radius: 5px;-fx-border-color: #1251af; -fx-border-width: 1px;-fx-border-radius: 5px; -fx-effect: dropshadow(gaussian, #238ac9, 10, 0.5, 0, 0);-fx-text-fill: white;-fx-font-weight: bold;"));
        changeButton.setOnMouseExited(e -> changeButton.setStyle("-fx-background-color: #62a0c5;-fx-background-radius: 5px;-fx-border-color: #1251af; -fx-border-width: 1px;-fx-border-radius: 5px;-fx-text-fill: white;-fx-font-weight: bold;"));
        changeButton.setPrefSize(125, 40);

        FlowPane listViewP = new FlowPane();
        listViewP.setPadding(new Insets(25,0,0,0));
        listViewP.setOrientation(Orientation.VERTICAL);

        itemsObservable = FXCollections.observableArrayList(mainController.getHabits());
        listViewTasks = new ListView<>(itemsObservable);
        listViewTasks.getSelectionModel().selectedItemProperty().addListener(mainController);
        listViewTasks.getSelectionModel().selectFirst();
        listViewTasks.addEventHandler(Event.ANY,mainController);
        listViewTasks.setPrefWidth(350);
        listViewTasks.setPrefHeight(500);
        listViewTasks.setStyle("-fx-background-color: #e4ebff;-fx-border-color: #7f7fab; -fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;-fx-text-fill: white;-fx-font-size: 15px;");

        comboBox = new ComboBox<>();
        String[] types = {"Daily Habit","Weekly Habit","OneTimeTask"};
        comboBox.getItems().addAll(types);
        comboBox.setStyle(
                "-fx-background-color: #315184;" +
                        "-fx-background-radius: 5px;" +
                        "-fx-border-color: #4a6691;" +
                        "-fx-border-radius: 5px;" +
                        "-fx-min-width: 200px;" +
                        "-fx-pref-height: 40px;"+
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 15px;"+
                        "-fx-border-width: 2px;"
        );
        comboBox.getSelectionModel().selectFirst();
        comboBox.setStyle(
                "-fx-background-color: #315184;" +
                        "-fx-background-radius: 5px;" +
                        "-fx-border-color: #4a6691;" +
                        "-fx-border-radius: 5px;" +
                        "-fx-min-width: 200px;" +
                        "-fx-pref-height: 40px;" +
                        "-fx-border-width: 2px;"
        );

        comboBox.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Arial'; -fx-font-size: 14px;");
                }
            }
        });

        comboBox.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                comboBox.applyCss();
                comboBox.layout();
                Node arrow = comboBox.lookup(".arrow");
                if (arrow != null) {
                    arrow.setStyle("-fx-background-color: #ffffff;");
                }
            }
        });

        comboBox.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-background-color: #315184;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #bdbdbd; -fx-background-color: #315184; -fx-font-family: 'Arial';");
                }
            }
        });


        nameTF.setPrefWidth(250);
        nameTF.setPrefHeight(40);
        nameTF.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        nameTF.setPromptText("Enter habit name");
        descriptionTF.setPrefHeight(40);
        descriptionTF.setPrefWidth(250);
        descriptionTF.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        descriptionTF.setPromptText("Enter habit description");
        pointsTF.setPrefHeight(40);
        pointsTF.setPrefWidth(250);
        pointsTF.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        pointsTF.setPromptText("Enter habit points");
        comboBox.setPrefHeight(40);
        comboBox.setPrefWidth(250);

        Label format = new Label("Habit List");
        format.setStyle("-fx-font-size: 25px;");
        listViewP.getChildren().addAll(format,listViewTasks);

        FlowPane controlButtons = new FlowPane();
        controlButtons.setHgap(10);

        controlButtons.getChildren().addAll(addButton,removeButton,changeButton);

        Label head = new Label("Create Habit");
        head.setStyle("-fx-font-size: 25px;-fx-font-style: bold;");
        gridPane.add(head,0,13);

        Label l1 = new Label("Name:");
        l1.setStyle("-fx-font-size: 15px");
        gridPane.add(l1, 0, 14);
        gridPane.add(nameTF, 1, 14);

        Label l2 = new Label("Description:");
        l2.setStyle("-fx-font-size: 15px");
        gridPane.add(l2, 0, 15);
        gridPane.add(descriptionTF, 1, 15);

        Label l3 = new Label("Points:");
        l3.setStyle("-fx-font-size: 15px");
        gridPane.add(l3, 0, 16);
        gridPane.add(pointsTF, 1, 16);

        Label l4 = new Label("Type:");
        l4.setStyle("-fx-font-size: 15px");
        gridPane.add(l4, 0, 17);
        gridPane.add(comboBox, 1, 17);

        gridPane.add(controlButtons, 0, 18, 2, 1);


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

    public void openNewWindow() {

        mainController = new MainController(this);

        Stage subStage = new Stage();
        subStage.setTitle("Info");

        String[] info = mainController.getInfo();

        TextField nameField = new TextField();
        nameField.setEditable(false);
        nameField.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        TextField descriptionField = new TextField();
        descriptionField.setEditable(false);
        descriptionField.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        TextField pointsField = new TextField();
        pointsField.setEditable(false);
        pointsField.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        TextField typeField = new TextField();
        typeField.setEditable(false);
        typeField.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        TextField streakField = new TextField();
        streakField.setEditable(false);
        streakField.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");
        TextField isCompletedField = new TextField();
        isCompletedField.setEditable(false);
        isCompletedField.setStyle("-fx-background-color: #2d5185;-fx-text-fill: white;-fx-font-size: 15px;-fx-border-color: #7f7fab;-fx-border-width: 2px;-fx-border-radius: 6px;-fx-background-radius: 6px;");

        if(!info[0].equals("O")){
            nameField.setText(info[1]);
            descriptionField.setText(info[2]);
            pointsField.setText(info[3]);
            if (info[0].equals("D")) {
                typeField.setText("Daily Habit");
            } else if (info[0].equals("W")) {
                typeField.setText("Weekly Habit");
            }
            else {
                typeField.setText("OneTimeTask");
            }
            streakField.setText(info[5]);
            isCompletedField.setText(info[4]);
        }
        else {
            nameField.setText(info[1]);
            descriptionField.setText(info[2]);
            pointsField.setText(info[3]);
            if (info[0].equals("D")) {
                typeField.setText("Daily Habit");
            } else if (info[0].equals("W")) {
                typeField.setText("Weekly Habit");
            }
            else {
                typeField.setText("OneTimeTask");
            }
            isCompletedField.setText(info[4]);
        }

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(25, 25, 25, 25));
        gridPane.add(new Label("Name:"), 0, 1);
        gridPane.add(nameField, 1, 1);
        gridPane.add(new Label("Description:"), 0, 2);
        gridPane.add(descriptionField, 1, 2);
        gridPane.add(new Label("Points:"), 0, 3);
        gridPane.add(pointsField, 1, 3);
        gridPane.add(new Label("Type:"), 0, 4);
        gridPane.add(typeField, 1, 4);
        if(!info[0].equals("O")) {
            gridPane.add(new Label("Streak:"), 0, 5);
            gridPane.add(streakField, 1, 5);
            gridPane.add(new Label("Completed:"), 0, 6);
            gridPane.add(isCompletedField, 1, 6);
        }
        else {
            gridPane.add(new Label("Completed:"), 0, 5);
            gridPane.add(isCompletedField, 1, 5);
        }


        VBox vbox = new VBox();
        Scene scene = new Scene(vbox, 325, 350);

        vbox.getChildren().add(gridPane);

        subStage.setScene(scene);
        subStage.show();
    }
}
