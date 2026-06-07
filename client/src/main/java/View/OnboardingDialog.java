package View;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class OnboardingDialog {
    private final Stage owner;
    private final Canvas avatar = new Canvas(320, 180);
    private String bodySize = "Medium";
    private String skinColor = "Warm";
    private String hairStyle = "Short";
    private final List<String> focusAreas = new ArrayList<>();

    public OnboardingDialog(Stage owner) {
        this.owner = owner;
    }

    public void show() {
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Create your hero");

        Label guide = new Label("Hello there! I am your Disciplica guide. Choose your hero and your first quest focus.");
        guide.getStyleClass().add("auth-subtitle");
        guide.setWrapText(true);

        avatar.getStyleClass().add("avatar-stage");
        drawAvatarPreview();

        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("onboarding-tabs");
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().add(new Tab("Body", optionGrid("Choose your body size", "body", "Small", "Medium", "Tall")));
        tabs.getTabs().add(new Tab("Skin", optionGrid("Choose your skin color", "skin", "Warm", "Tan", "Brown", "Fantasy")));
        tabs.getTabs().add(new Tab("Hair", optionGrid("Choose your hair style", "hair", "Short", "Long", "Bangs", "Spikes")));
        tabs.getTabs().add(new Tab("Focus", focusGrid()));

        Button finish = new Button("Finish");
        finish.getStyleClass().addAll("habitica-button", "success");
        finish.setPrefWidth(160);
        finish.setOnAction(event -> dialog.close());

        HBox actionBar = new HBox(finish);
        actionBar.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(18, guide, avatar, tabs, actionBar);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(28));
        card.getStyleClass().add("auth-card");

        Scene scene = new Scene(card, 760, 720);
        var css = getClass().getResource("/css/habitica-theme.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private GridPane optionGrid(String titleText, String category, String... options) {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("onboarding-option-grid");
        grid.setHgap(14);
        grid.setVgap(16);
        grid.setPadding(new Insets(20, 8, 20, 8));

        Label title = new Label(titleText);
        title.getStyleClass().add("onboarding-section-title");
        grid.add(title, 0, 0, options.length, 1);

        List<Button> buttons = new ArrayList<>();
        for (int index = 0; index < options.length; index++) {
            String option = options[index];
            Button button = new Button(option);
            button.getStyleClass().addAll("habitica-button", "avatar-option-button");
            button.setPrefWidth(140);
            button.setMinHeight(44);
            button.setOnAction(event -> {
                setSelection(category, option);
                updateSelectedButtons(buttons, option);
                drawAvatarPreview();
            });
            buttons.add(button);
            grid.add(button, index, 1);
        }
        updateSelectedButtons(buttons, currentSelection(category));
        return grid;
    }

    private GridPane focusGrid() {
        GridPane grid = new GridPane();
        grid.getStyleClass().add("onboarding-option-grid");
        grid.setHgap(28);
        grid.setVgap(16);
        grid.setPadding(new Insets(20, 8, 20, 8));

        Label title = new Label("Choose what you want to work on");
        title.getStyleClass().add("onboarding-section-title");
        grid.add(title, 0, 0, 2, 1);

        String[] labels = {"Work", "Exercise", "Health & Wellness", "School", "Chores", "Creativity", "Self-Care"};
        for (int index = 0; index < labels.length; index++) {
            String label = labels[index];
            CheckBox checkBox = new CheckBox(label);
            checkBox.getStyleClass().add("onboarding-checkbox");
            checkBox.setSelected(focusAreas.contains(label));
            checkBox.selectedProperty().addListener((observable, wasSelected, isSelected) -> {
                if (isSelected && !focusAreas.contains(label)) {
                    focusAreas.add(label);
                } else if (!isSelected) {
                    focusAreas.remove(label);
                }
            });
            grid.add(checkBox, index % 2, 1 + index / 2);
        }
        return grid;
    }

    private void setSelection(String category, String option) {
        switch (category) {
            case "body" -> bodySize = option;
            case "skin" -> skinColor = option;
            case "hair" -> hairStyle = option;
            default -> throw new IllegalArgumentException("Unknown avatar option category: " + category);
        }
    }

    private String currentSelection(String category) {
        return switch (category) {
            case "body" -> bodySize;
            case "skin" -> skinColor;
            case "hair" -> hairStyle;
            default -> "";
        };
    }

    private void updateSelectedButtons(List<Button> buttons, String selectedOption) {
        for (Button button : buttons) {
            boolean selected = button.getText().equals(selectedOption);
            button.getStyleClass().remove("selected");
            if (selected) {
                button.getStyleClass().add("selected");
            }
        }
    }

    private void drawAvatarPreview() {
        GraphicsContext graphics = avatar.getGraphicsContext2D();
        graphics.clearRect(0, 0, avatar.getWidth(), avatar.getHeight());

        graphics.setFill(Color.web("#d8ecff"));
        graphics.fillRect(0, 0, avatar.getWidth(), avatar.getHeight());
        graphics.setFill(Color.web("#96cf7b"));
        graphics.fillOval(54, 106, 210, 48);

        double bodyHeight = switch (bodySize) {
            case "Small" -> 42;
            case "Tall" -> 72;
            default -> 56;
        };
        double bodyY = 92 - (bodyHeight - 56);
        double headY = bodyY - 26;
        double centerX = 160;

        Color skin = switch (skinColor) {
            case "Tan" -> Color.web("#d79055");
            case "Brown" -> Color.web("#7a4b2a");
            case "Fantasy" -> Color.web("#82d8ce");
            default -> Color.web("#f2c8a0");
        };
        Color hair = switch (hairStyle) {
            case "Long" -> Color.web("#3a2a22");
            case "Bangs" -> Color.web("#d86426");
            case "Spikes" -> Color.web("#20242a");
            default -> Color.web("#a94f22");
        };

        graphics.setFill(Color.web("#7c5ee6"));
        graphics.fillRect(centerX - 18, bodyY, 36, bodyHeight);
        graphics.setFill(Color.web("#2f214d"));
        graphics.fillRect(centerX - 16, bodyY + bodyHeight, 9, 28);
        graphics.fillRect(centerX + 7, bodyY + bodyHeight, 9, 28);

        graphics.setFill(skin);
        graphics.fillRect(centerX - 14, headY, 28, 24);

        graphics.setFill(hair);
        if ("Long".equals(hairStyle)) {
            graphics.fillRect(centerX - 19, headY - 8, 38, 36);
        } else if ("Bangs".equals(hairStyle)) {
            graphics.fillRect(centerX - 18, headY - 7, 36, 14);
            graphics.fillRect(centerX - 8, headY + 1, 18, 8);
        } else if ("Spikes".equals(hairStyle)) {
            graphics.fillPolygon(
                    new double[]{centerX - 20, centerX - 12, centerX - 4, centerX + 6, centerX + 17},
                    new double[]{headY + 2, headY - 12, headY + 2, headY - 10, headY + 2},
                    5
            );
            graphics.fillRect(centerX - 18, headY, 36, 10);
        } else {
            graphics.fillRect(centerX - 18, headY - 7, 36, 12);
            graphics.fillRect(centerX - 12, headY - 14, 24, 8);
        }

        graphics.setFill(Color.web("#19142b"));
        graphics.fillRect(centerX - 8, headY + 9, 4, 4);
        graphics.fillRect(centerX + 6, headY + 9, 4, 4);

        if ("Fantasy".equals(skinColor)) {
            graphics.setFill(Color.web("#4fcf5a"));
            graphics.fillOval(centerX + 24, bodyY + 20, 14, 14);
        }
    }
}
