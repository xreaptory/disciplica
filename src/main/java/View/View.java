package View;

import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.*;

import model.Properties;

public class View extends Stage{


    // menu items
    final MenuItem menuCloseMI, menuNewAppMI;

    // button
    final Button checkBTN;

    // textfield
    final TextField numberTF;

    // reference to the controller
    final private Controller simpleController;

    /**
     *
     */
    public View(){

        // instanciate controller for action, mouse and key events
        // the controller will be added to the components
        simpleController =new Controller(this);

        // top level pane: includes menubar + borderpane
        VBox vBox=new VBox();
        // vBox.setPrefWidth(300);
        // borderpane
        BorderPane borderPane=new BorderPane();
        // set Background color
        borderPane.setBackground(new Background(new BackgroundFill(Properties.ERMBackgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));
        // BorderPane auf volle Breite setzen
        // borderPane.setPrefWidth(Double.MAX_VALUE);
        // with
        //borderPane.setPrefWidth(500);
        // mit nachfolgenden Befehl kann sich die BorderPane vertikal/horizontal ausbreiten
        VBox.setVgrow(borderPane, Priority.ALWAYS);

        // gridpane contains rows and columns
        GridPane gridPane=new GridPane();
        gridPane.setPadding(new Insets(10,10,10,10));

        // gap between the components
        gridPane.setHgap(10);
        gridPane.setVgap(10);

        numberTF=new TextField();
        numberTF.addEventHandler(Event.ANY, simpleController);
        numberTF.addEventFilter(KeyEvent.KEY_TYPED, simpleController);

        numberTF.setFont(Properties.sevenSegmentFont);
        numberTF.setTooltip(new Tooltip("Only number allowed"));

        gridPane.add(new Label("Number to check:"),0,0);
        gridPane.add(numberTF,1,0);

        //flowpane for buttons
        FlowPane buttonPane=new FlowPane();
        buttonPane.setPadding(new Insets(5,5,5,5));
        buttonPane.setAlignment(Pos.CENTER);

        // Button for action
        checkBTN=new Button("Check number prim");
        checkBTN.setTooltip(new Tooltip("Check if number is prim"));
        checkBTN.addEventHandler(Event.ANY, simpleController);

        buttonPane.getChildren().add(checkBTN);

        // add panes to borderpane
        borderPane.setCenter(gridPane);
        borderPane.setBottom(buttonPane);

        // create and add a menu
        MenuBar menuBar = new MenuBar();
        // Menu file, the underscore defines the following character for mnemonic
        // there is method setMnemonicParsing - default is true
        Menu menuFile = new Menu("_File");

        // menu close
        menuCloseMI = new MenuItem("_Close");
        // add controller
        menuCloseMI.addEventHandler(Event.ANY, simpleController);
        menuCloseMI.setMnemonicParsing(true);
        menuCloseMI.setAccelerator(
                KeyCombination.keyCombination("SHORTCUT+C")
        );
        // add menu items to menu file
        menuFile.getItems().addAll(menuCloseMI);
        // menu window
        Menu menuWindow = new Menu("_Window");
        // menu default
        menuNewAppMI = new MenuItem("New app");
        // add controller
        menuNewAppMI.addEventHandler(Event.ANY, simpleController);
        menuWindow.getItems().addAll(menuNewAppMI);
        // add menus to menu bar
        menuBar.getMenus().addAll(menuFile,menuWindow);

        // add menu bar and borderpane to vbox
        vBox.getChildren().addAll(menuBar,borderPane);

        // set properties of the frame
        setTitle("View");
        // set application icon
        getIcons().add(Properties.applicationImageIconAsICO);

        // set the scene and add vertical box to the scene
        Scene scene=new Scene(vBox);
        setScene(scene);

        // different appearance
        //initStyle(StageStyle.UTILITY);
        setResizable(true);

        // close behavior x pressed
        // lamda notation Verwendung mit funktionalen Interfaces (= Interface mit nur einer Methode)
        this.setOnCloseRequest(event-> {
            event.consume();
        });

        // show frame
        sizeToScene();
        centerOnScreen();
        show();

    }

}
