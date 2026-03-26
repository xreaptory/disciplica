package View;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.input.*;
import model.Model;
import model.Properties;
import model.SCTools;

public class Controller implements EventHandler<Event>{

    // reference to view
    final private View simpleView;
    final private Model model;
    /**
     * @param simpleView
     */
    public Controller(View simpleView, Model model) {
        this.simpleView = simpleView;
        this.model = model;
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
            Alert alert = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Do you really want to close?",
                    new ButtonType("OK", ButtonBar.ButtonData.OK_DONE),
                    new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE)
            );
            alert.initOwner(simpleView);
            ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);
            if (result.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                Platform.exit();
            } else {
                event.consume();
            }
        }

        if (source == simpleView.checkBTN) {
            handlePrimeCheck();
        }

    }// handle

    private void handlePrimeCheck() {
        String input = simpleView.numberTF.getText();
        if (input == null || input.isBlank()) {
            showInfo("Input required", "Please enter a number.");
            return;
        }

        Integer value = model.parseInteger(input.trim());
        if (value == null) {
            showInfo("Invalid number", "Only whole numbers are allowed.");
            return;
        }

        boolean isPrime = model.isPrime(value);
        String message = isPrime
                ? value + " is a prime number."
                : value + " is not a prime number.";
        showInfo("Prime Check Result", message);
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.initOwner(simpleView);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

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
                SCTools.BorderToNode(node, Properties.FocusOnComponentColor, 10);
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
            if (Character.isLetterOrDigit(c) && c != 8 && c != 127) {
                if (!Character.isDigit(c)) { // Allow digits
                    showInfo("Enter a number", "Only numbers are allowed.");
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
