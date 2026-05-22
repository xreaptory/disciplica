package com.disciplica;

import View.View;
import com.disciplica.di.TestModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ViewUiTest extends ApplicationTest {

    @Override
    public void start(Stage stage) {
        Injector injector = Guice.createInjector(new TestModule());
        new View(injector);
    }

    @BeforeEach
    void openHabitScreen(FxRobot robot) {
        robot.clickOn("Habits");
    }

    @Test
    void windowOpensWithCorrectTitle() {
        Stage stage = (Stage) listTargetWindows().get(0);
        assertEquals("Disciplica", stage.getTitle());
    }

    @Test
    void clickHabitsShowsAddHabitControls(FxRobot robot) {
        Button addButton = robot.lookup("Add").queryButton();
        assertTrue(addButton.isVisible());
        assertTrue(!addButton.isDisabled());
    }

    @Test
    void fillHabitFormAndSaveAppearsInList(FxRobot robot) {
        TextField nameField = robot.lookup(".text-field").nth(0).queryAs(TextField.class);
        TextField descriptionField = robot.lookup(".text-field").nth(1).queryAs(TextField.class);
        TextField pointsField = robot.lookup(".text-field").nth(2).queryAs(TextField.class);
        @SuppressWarnings("unchecked")
        ComboBox<String> typeCombo = robot.lookup(".combo-box").queryAs(ComboBox.class);

        clickAndType(robot, nameField, "Morning Run");
        clickAndType(robot, descriptionField, "Run 5km");
        clickAndType(robot, pointsField, "10");
        typeCombo.getSelectionModel().select("Daily Habit");

        robot.clickOn("Add");

        @SuppressWarnings("unchecked")
        ListView<String> listView = robot.lookup(".list-view").queryAs(ListView.class);
        assertTrue(listView.getItems().stream().anyMatch(item -> item.contains("Morning Run")));
    }

    @Test
    void completeHabitIncreasesStreak(FxRobot robot) {
        addDailyHabit(robot, "Hydrate", "Drink water", "5");

        @SuppressWarnings("unchecked")
        ListView<String> listView = robot.lookup(".list-view").queryAs(ListView.class);
        String before = listView.getItems().stream().filter(i -> i.contains("Hydrate")).findFirst().orElse("");
        assertTrue(before.endsWith(";0"));

        robot.doubleClickOn("Hydrate");
        robot.clickOn("Complete");

        String after = listView.getItems().stream().filter(i -> i.contains("Hydrate")).findFirst().orElse("");
        assertTrue(after.endsWith(";1"));
    }

    @Test
    void saveButtonExistsAndIsClickable(FxRobot robot) {
        Button saveButton = robot.lookup("Save").queryButton();
        assertTrue(saveButton.isVisible());
        robot.clickOn(saveButton);
    }

    private void addDailyHabit(FxRobot robot, String name, String description, String points) {
        TextField nameField = robot.lookup(".text-field").nth(0).queryAs(TextField.class);
        TextField descriptionField = robot.lookup(".text-field").nth(1).queryAs(TextField.class);
        TextField pointsField = robot.lookup(".text-field").nth(2).queryAs(TextField.class);

        clickAndType(robot, nameField, name);
        clickAndType(robot, descriptionField, description);
        clickAndType(robot, pointsField, points);
        robot.clickOn("Add");
    }

    private void clickAndType(FxRobot robot, TextField field, String text) {
        robot.clickOn(field);
        robot.eraseText(field.getText().length());
        robot.write(text);
    }
}
