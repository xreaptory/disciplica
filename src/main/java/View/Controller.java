package View;


import at.spengergasse.mvc_muster.model.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.input.*;
import model.SCTools;

import java.util.Optional;
import java.util.Properties;

public class Controller implements EventHandler<Event>{

    // reference to view
    final private View simpleView;

    final private Properties properties;
    /**
     * @param simpleView
     */
    public Controller(View simpleView) {
        this.simpleView = simpleView;
        // Liste der
        this.properties = properties;
    }


    /**
     * Handle action events
     *
     * @param event
     */
    public void handleActionEvent(ActionEvent event) {
        // get source of the event
        Object source = event.getSource();

        //**********************************************************************
        // open new frame with same functionality
        if (source == simpleView.menuNewAppMI) {
            new View();
        }

        //**********************************************************************
        // close
        if (source == simpleView.menuCloseMI) {
            MyAlertFX  alert=new MyAlertFX(simpleView,
                    Alert.AlertType.CONFIRMATION,
                    "Close Window",
                    "",
                    "Do you really want to close",
                    true,
                    Properties.applicationImageIconAsICO,
                    "OK",
                    "ESC->Cancel",
                    Properties.ButtonBackgroundColor,
                    Properties.MouseSelectedColor,
                    Properties.FocusOnComponentColor);
            // wait for selection
            Optional<ButtonType> result = alert.getResult();

            // if cancel return
            if (result.isPresent() && result.get() == ButtonType.OK){
                Platform.exit();
            }
            else{
                event.consume();
            }

        }

    }// handle

    /**
     * Handles mouse events
     *
     * @param event
     */
    private void handleMouseEvent(MouseEvent event) {
        // get source of the event
        Object source = event.getSource();

        if (source instanceof Node) {
            Node node = (Node) source;
            if (event.getEventType()==MouseEvent.MOUSE_ENTERED){
                SCTools.BorderToNode(node, Properties.FocusOnComponentColor(), 10);
                node.requestFocus();
                if (node instanceof Button)
                    simpleView.getScene().getRoot().setCursor(Cursor.HAND);
            }

            if (event.getEventType()==MouseEvent.MOUSE_EXITED) {
                node.setEffect(null);
                simpleView.getScene().getRoot().setCursor(Cursor.DEFAULT);
            }
        }
    }

    /**
     * Handle key events
     *
     * @param event
     */
    private void handleKeyEvent(KeyEvent event){
        // get source of the event
        Object source = event.getSource();

        if (source==simpleView.numberTF&&event.getEventType()==KeyEvent.KEY_TYPED) {
            char c = event.getCharacter().charAt(0);
            // backspace or delete are allowed
            if (Character.isLetterOrDigit(c)&&(c!=8||c==127)) {
                if (!Character.isDigit(c)) { // Allow digits
                    new MyAlertFX(simpleView,
                            Alert.AlertType.INFORMATION,
                            "Enter a number to check prim",
                            "",
                            "Only numbers allowed " + event.getCode(),
                            true,
                            Properties.applicationImageIconAsICO,
                            "OK",
                            "",
                            Properties.ButtonBackgroundColor,
                            Properties.MouseSelectedColor,
                            Properties.FocusOnComponentColor);
                    event.consume(); // Ignore the event
                }
            }
        }

        if (source== simpleView.numberTF&&event.getEventType()==KeyEvent.KEY_PRESSED) {
            switch (event.getCode()) {
                case DELETE: System.out.println("Delete pressed");break;
                case ENTER: System.out.println("Enter pressed");
                    simpleView.checkBTN.requestFocus();
                    break;
            }
        }
    }

    /**
     * Calls methode depending on the used action
     *
     * @param event
     */
    @Override
    public void handle(Event event) {
        // in case of ActionEvent
        if (event instanceof ActionEvent) {
            handleActionEvent((ActionEvent) event);
        }
        // in case of MouseEvent
        if (event instanceof MouseEvent) {
            handleMouseEvent((MouseEvent) event);
        }
        // in case of KeyEvent
        if (event instanceof KeyEvent) {
            handleKeyEvent((KeyEvent) event);
        }
    }// handle

}
