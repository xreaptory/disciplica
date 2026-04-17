package View;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import model.SCTools;

import java.io.InputStream;
import java.util.Optional;

public class MyAlertFX {

    private Optional<ButtonType> result;
    private Alert alert;

    public MyAlertFX(
            Window window,
            AlertType alertType,
            String title,
            String header,
            String contentText,
            Boolean showAndWait,
            Image image,
            String okBtnText,
            String cancelBtnText,
            Color btnBackgroundColor,
            Color btnSelectedColor,
            Color btnFocusColor) {

        alert = setAlert(window, alertType, title, header, contentText, image);

        Button button = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        if (button != null) {
            setButtonLookAndFeel(button, okBtnText, btnBackgroundColor, btnSelectedColor, btnFocusColor);
        }

        button = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
        if (button != null) {
            setButtonLookAndFeel(button, cancelBtnText, btnBackgroundColor, btnSelectedColor, btnFocusColor);
        }

        if (showAndWait) {
            alert.setOnShown(event -> {
                double maxWidth = 0;

                for (ButtonType type : alert.getButtonTypes()) {
                    Button current = (Button) alert.getDialogPane().lookupButton(type);
                    current.applyCss();
                    current.layout();
                    maxWidth = Math.max(maxWidth, current.prefWidth(-1));
                }

                for (ButtonType type : alert.getButtonTypes()) {
                    Button current = (Button) alert.getDialogPane().lookupButton(type);
                    current.setMinWidth(maxWidth + 20);
                }
            });

            result = alert.showAndWait();
        } else {
            alert.show();
        }
    }

    private void setButtonLookAndFeel(
            final Button button,
            String buttonText,
            Color btnBackgroundColor,
            Color btnSelectedColor,
            Color btnFocusColor) {

        button.setBackground(new Background(new BackgroundFill(btnBackgroundColor, new CornerRadii(5.0), Insets.EMPTY)));
        button.setOnMouseEntered(new EventHandler<>() {
            @Override
            public void handle(MouseEvent event) {
                button.getScene().getRoot().setCursor(Cursor.HAND);
                SCTools.BorderToNode(button, btnSelectedColor, 10);
            }
        });
        button.setOnMouseExited(new EventHandler<>() {
            @Override
            public void handle(MouseEvent event) {
                button.getScene().getRoot().setCursor(Cursor.DEFAULT);
                if (button.isFocused()) {
                    SCTools.BorderToNode(button, btnFocusColor, 10);
                } else {
                    button.setEffect(null);
                }
            }
        });
        button.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (button.isFocused()) {
                SCTools.BorderToNode(button, btnFocusColor, 10);
            } else {
                button.setEffect(null);
            }
        });

        if (buttonText != null) {
            button.setText(buttonText);
        }
    }

    private Alert setAlert(
            Window window,
            AlertType alertType,
            String title,
            String header,
            String contentText,
            Image image) {

        alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(contentText);
        alert.initOwner(window);
        alert.initStyle(StageStyle.DECORATED);

        ImageView iconView = null;
        switch (alertType) {
            case ERROR:
                iconView = toImageView(safeLoadImage("/images/alerterror.png"));
                break;
            case INFORMATION:
                iconView = toImageView(safeLoadImage("/images/alertinformation.png"));
                break;
            case CONFIRMATION:
                iconView = toImageView(safeLoadImage("/images/alertconfirmation.png"));
                break;
            default:
                break;
        }
        if (iconView != null) {
            iconView.setFitWidth(30);
            iconView.setFitHeight(30);
            alert.setGraphic(iconView);
        }

        alert.getDialogPane().getChildren().stream()
                .filter(node -> node instanceof Label)
                .forEach(node -> ((Label) node).setMinHeight(Region.USE_PREF_SIZE));
        alert.getDialogPane().getChildren().stream()
                .filter(node -> node instanceof Label)
                .forEach(node -> ((Label) node).setMinWidth(Region.USE_PREF_SIZE));
        alert.getDialogPane().getChildren().stream()
                .filter(node -> node instanceof Label)
                .forEach(node -> ((Label) node).setMaxWidth(Region.USE_PREF_SIZE));

        alert.getDialogPane().setBackground(new Background(new BackgroundFill(Color.GAINSBORO, CornerRadii.EMPTY, Insets.EMPTY)));

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        if (image != null) {
            stage.getIcons().add(image);
        }
        stage.setOnCloseRequest(new EventHandler<>() {
            @Override
            public void handle(WindowEvent event) {
                event.consume();
            }
        });
        stage.centerOnScreen();
        stage.setAlwaysOnTop(true);

        return alert;
    }

    private Image safeLoadImage(String resourcePath) {
        InputStream stream = getClass().getResourceAsStream(resourcePath);
        if (stream == null) {
            return null;
        }
        return new Image(stream);
    }

    private ImageView toImageView(Image image) {
        return image == null ? null : new ImageView(image);
    }

    public Optional<ButtonType> getResult() {
        return result;
    }

    public Alert getAlert() {
        return alert;
    }
}