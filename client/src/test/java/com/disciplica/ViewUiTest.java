package com.disciplica;

import View.View;
import com.disciplica.di.TestModule;
import com.disciplica.testtags.UITest;
import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UITest
class ViewUiTest extends ApplicationTest {

    @RegisterExtension
    static UiFailureScreenshotExtension screenshots = new UiFailureScreenshotExtension();

    @BeforeAll
    static void setupHeadless() {
        System.setProperty("testfx.robot", "glass");
        System.setProperty("testfx.headless", "true");
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
        System.setProperty("prism.order", "sw");
        System.setProperty("java.awt.headless", "true");
    }

    @Override
    public void start(Stage stage) {
        Injector injector = Guice.createInjector(new TestModule());
        new View(injector);
    }

    @Test
    @DisplayName("Launch app and verify window title")
    void launchAppVerifyWindowTitle() {
        Stage primary = (Stage) listTargetWindows().get(0);
        assertEquals("Disciplica", primary.getTitle());
    }

    @Test
    @DisplayName("Click Add Habit, fill form, save, and verify it appears in list")
    void addHabitFlow() {
        clickOn("Habits");
        addDailyHabit("Morning Run", "Run 5km", "10");

        @SuppressWarnings("unchecked")
        ListView<String> listView = lookup(".list-view").queryAs(ListView.class);
        assertTrue(listView.getItems().stream().anyMatch(item -> item.contains("Morning Run")));
    }

    @Test
    @DisplayName("Complete habit and verify XP bar increases")
    void completeHabitIncreasesXpBar(FxRobot robot) {
        clickOn("Habits");
        addDailyHabit("Hydrate", "Drink water", "12");

        clickOn("Stats");
        ProgressBar xpBarBefore = lookup("#xpProgressBar").queryAs(ProgressBar.class);
        double before = xpBarBefore.getProgress();

        clickOn("Habits");
        robot.doubleClickOn("Hydrate");
        clickOn("Complete");
        closeAnyInfoAlertIfPresent(robot);

        clickOn("Stats");
        ProgressBar xpBarAfter = lookup("#xpProgressBar").queryAs(ProgressBar.class);
        double after = xpBarAfter.getProgress();
        assertTrue(after > before, "XP progress bar should increase after completing a habit");
    }

    @Test
    @DisplayName("Navigate to dashboard and verify chart renders")
    void navigateToStatsAndVerifyChartRenders() {
        clickOn("Stats");
        clickOn("Dashboard");
        WaitForAsyncUtils.waitForFxEvents();

        BarChart<?, ?> completionsChart = lookup("#dashboardCompletionsChart").queryAs(BarChart.class);
        assertNotNull(completionsChart);
        assertTrue(!completionsChart.getData().isEmpty(), "Completions chart should have series data");

        Node xpChart = lookup("#dashboardXpChart").query();
        assertNotNull(xpChart);
    }

    private void addDailyHabit(String name, String description, String points) {
        TextField nameField = lookup(".text-field").nth(0).queryAs(TextField.class);
        TextField descriptionField = lookup(".text-field").nth(1).queryAs(TextField.class);
        TextField pointsField = lookup(".text-field").nth(2).queryAs(TextField.class);
        @SuppressWarnings("unchecked")
        ComboBox<String> typeCombo = lookup(".combo-box").queryAs(ComboBox.class);

        clickAndType(nameField, name);
        clickAndType(descriptionField, description);
        clickAndType(pointsField, points);
        typeCombo.getSelectionModel().select("Daily Habit");
        clickOn("Add");
    }

    private void closeAnyInfoAlertIfPresent(FxRobot robot) {
        WaitForAsyncUtils.sleep(150, TimeUnit.MILLISECONDS);
        if (!robot.lookup(".dialog-pane").queryAll().isEmpty()) {
            robot.clickOn("OK");
        }
    }

    private void clickAndType(TextField field, String text) {
        clickOn(field);
        eraseText(field.getText().length());
        write(text);
    }
}
