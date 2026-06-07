package View;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import model.service.UserService;
import model.domain.exception.HabitNotFoundException;
import model.domain.exception.InvalidHabitException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import model.domain.model.*;
import View.api.SessionStore;
import com.disciplica.shared.task.CreateTaskRequest;
import com.disciplica.shared.task.TaskDto;
import com.disciplica.shared.task.TaskType;
import com.disciplica.shared.task.UpdateTaskRequest;
import com.disciplica.shared.user.UserProfile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainController implements EventHandler<Event>, ChangeListener<String> {

    final User user;
    private final UserService userService;
    private final SessionStore sessionStore;
    private final Map<String, UUID> hostedTaskIds = new HashMap<>();

    final private View simpleView;
    private UserStatsSnapshot cachedStats;
    private long cachedStatsNanos;
    private static final long STATS_CACHE_TTL_NANOS = 1_000_000_000L;

    public MainController(View simpleView, UserService userService) {
        this(simpleView, userService, null);
    }

    public MainController(View simpleView, UserService userService, SessionStore sessionStore) {
        this.userService = userService;
        this.sessionStore = sessionStore;
        user = userService.getUser();
        this.simpleView = simpleView;
    }

    public static int calculateHabitPoints(String category, int minutes, String type) {
        int safeMinutes = Math.max(5, Math.min(480, minutes));
        double base = safeMinutes * 0.8;
        double categoryMultiplier = switch (category == null ? "" : category) {
            case "Health" -> 1.2;
            case "Learning" -> 1.25;
            case "Fitness" -> 1.35;
            case "Chores" -> 0.95;
            default -> 1.0; // Work
        };
        double typeMultiplier = switch (type == null ? "" : type) {
            case "Weekly Habit" -> 1.35;
            case "OneTimeTask" -> 1.15;
            default -> 1.0; // Daily Habit
        };
        return Math.max(1, (int) Math.round(base * categoryMultiplier * typeMultiplier));
    }

    public void handleActionEvent(ActionEvent event) throws InvalidHabitException, HabitNotFoundException, IOException {
        Object source = event.getSource();

        if(source == simpleView.habitsBTN){
            simpleView.openHabitMenu();
        }

        if(source == simpleView.statsBTN){
            simpleView.openStats();
        }

        if(source == simpleView.partyBTN){
            simpleView.openParty();
        }

        if(source == simpleView.dashboardBTN){
            simpleView.openDashboard();
        }

        if (source == simpleView.saveButton) {
            saveAllAsync(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Data saved successfully.");
                alert.showAndWait();
            });
        }

        if (source == simpleView.addButton) {
            if (useHostedTasks()) {
                createHostedTask();
                return;
            }
            String typ = simpleView.comboBox.getSelectionModel().getSelectedItem().toString();
            if (typ.equals("Daily Habit")) {
                try {
                    DailyHabit dailyHabit = new DailyHabit(simpleView.nameTF.getText(), simpleView.descriptionTF.getText(), Integer.parseInt(simpleView.pointsTF.getText()));
                    user.addTask(dailyHabit);
                    invalidateStatsCache();
                    simpleView.itemsObservable.setAll(user.getAllHabits());
                    simpleView.refreshDashboardData();
                    persistAllDataSafely();
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Error parsing points. Please enter a valid integer.");
                }
            } else if (typ.equals("Weekly Habit")) {
                try {
                    WeeklyHabit weeklyHabit = new WeeklyHabit(simpleView.nameTF.getText(), simpleView.descriptionTF.getText(), Integer.parseInt(simpleView.pointsTF.getText()));
                    user.addTask(weeklyHabit);
                    invalidateStatsCache();
                    simpleView.itemsObservable.setAll(user.getAllHabits());
                    simpleView.refreshDashboardData();
                    persistAllDataSafely();
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Error parsing points. Please enter a valid integer.");
                }
            } else if (typ.equals("OneTimeTask")) {
                try {
                    OneTimeTask oneTimeTask = new OneTimeTask(simpleView.nameTF.getText(), simpleView.descriptionTF.getText(), Integer.parseInt(simpleView.pointsTF.getText()));
                    user.addTask(oneTimeTask);
                    invalidateStatsCache();
                    simpleView.itemsObservable.setAll(user.getAllHabits());
                    simpleView.refreshDashboardData();
                    persistAllDataSafely();
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Error parsing points. Please enter a valid integer.");
                }
            }
        }

        if (source == simpleView.removeButton) {
            if (useHostedTasks()) {
                deleteHostedTask();
                return;
            }
            String typ = simpleView.comboBox.getSelectionModel().getSelectedItem().toString();
            if (typ.equals("Daily Habit")) {
                try {
                    DailyHabit dailyHabit = new DailyHabit(simpleView.nameTF.getText(), simpleView.descriptionTF.getText(), Integer.parseInt(simpleView.pointsTF.getText()));
                    user.removeTask(dailyHabit);
                    invalidateStatsCache();
                    simpleView.itemsObservable.setAll(user.getAllHabits());
                    simpleView.refreshDashboardData();
                    persistUserStatsSafely();
                } catch (HabitNotFoundException e) {
                    throw new HabitNotFoundException("Habit not found. Please check the name, description, and points.");
                }
            } else if (typ.equals("Weekly Habit")) {
                try {
                    WeeklyHabit weeklyHabit = new WeeklyHabit(simpleView.nameTF.getText(), simpleView.descriptionTF.getText(), Integer.parseInt(simpleView.pointsTF.getText()));
                    user.removeTask(weeklyHabit);
                    invalidateStatsCache();
                    simpleView.itemsObservable.setAll(user.getAllHabits());
                    simpleView.refreshDashboardData();
                    persistUserStatsSafely();
                } catch (HabitNotFoundException e) {
                    throw new HabitNotFoundException("Habit not found. Please check the name, description, and points.");
                }
            } else if (typ.equals("OneTimeTask")) {
                try {
                    OneTimeTask oneTimeTask = new OneTimeTask(simpleView.nameTF.getText(), simpleView.descriptionTF.getText(), Integer.parseInt(simpleView.pointsTF.getText()));
                    user.removeTask(oneTimeTask);
                    invalidateStatsCache();
                    simpleView.itemsObservable.setAll(user.getAllHabits());
                    simpleView.refreshDashboardData();
                    persistUserStatsSafely();
                } catch (HabitNotFoundException e) {
                    throw new HabitNotFoundException("Habit not found. Please check the name, description, and points.");
                }
            }
        }

        if (source == simpleView.changeButton) {
            if (useHostedTasks()) {
                updateHostedTask();
                return;
            }
            try {
                String selectedItem = simpleView.listViewTasks.getSelectionModel().getSelectedItem();
                if (selectedItem == null) return;

                String[] oldParts = selectedItem.split(";");
                String oldTypLetter = oldParts[0];
                String oldName = oldParts[1];

                AbstractTask oldTask = user.getTaskName(oldName);

                if (oldTask == null) return;

                String typNew = simpleView.comboBox.getSelectionModel().getSelectedItem().toString();
                String name = simpleView.nameTF.getText();
                String desc = simpleView.descriptionTF.getText();
                int points = Integer.parseInt(simpleView.pointsTF.getText());

                AbstractTask newTask;
                if (typNew.equals("Daily Habit")) {
                    newTask = new DailyHabit(name, desc, points);
                } else if (typNew.equals("Weekly Habit")) {
                    newTask = new WeeklyHabit(name, desc, points);
                } else {
                    newTask = new OneTimeTask(name, desc, points);
                }

                newTask.setCompleted(oldTask.isCompleted());
                if (newTask instanceof DailyHabit && oldTask instanceof DailyHabit) {
                    ((DailyHabit) newTask).setStreak(((DailyHabit) oldTask).getStreak());
                } else if (newTask instanceof WeeklyHabit && oldTask instanceof WeeklyHabit) {
                    ((WeeklyHabit) newTask).setStreak(((WeeklyHabit) oldTask).getStreak());
                }

                user.changeTask(oldTask, newTask);
                invalidateStatsCache();

                simpleView.itemsObservable.setAll(user.getAllHabits());
                simpleView.refreshDashboardData();
                persistUserStatsSafely();

            } catch (InvalidHabitException | HabitNotFoundException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Fehler beim Ändern: " + e.getMessage());
                alert.showAndWait();
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Punkte müssen eine Zahl sein!");
                alert.showAndWait();
            }
        }

        if(source == simpleView.completeButton){
            if (useHostedTasks()) {
                completeHostedTask();
                return;
            }
            try{
                user.printTasks();

                String searchedName = simpleView.nameField.getText();
                String typ = simpleView.typeField.getText();
                AbstractTask task = user.getTaskName(searchedName);


                if(task == null) {
                    throw new HabitNotFoundException("Habit with name '" + searchedName + "' not found. Please check the name.");
                }

                user.completeTask(task);
                invalidateStatsCache();
                if(typ.equals("OneTimeTask")) {
                    user.removeTask(task);
                    invalidateStatsCache();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Aufgabe abgeschlossen");
                    alert.setHeaderText("Aufgabe '" + searchedName + "' abgeschlossen und entfernt!");
                    alert.setContentText("Du hast " + task.calculatePoints() + " Punkte erhalten!");
                    alert.showAndWait();
                }
                else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Aufgabe abgeschlossen");
                    alert.setHeaderText("Aufgabe '" + searchedName + "' abgeschlossen!");
                    alert.setContentText("Du hast " + task.calculatePoints() + " Punkte erhalten! Streak: " + task.getStreak());
                    alert.showAndWait();
                }

                simpleView.itemsObservable.setAll(user.getAllHabits());
                simpleView.refreshDashboardData();
                persistUserStatsSafely();

                ((Stage)simpleView.completeButton.getScene().getWindow()).close();
            }
            catch (HabitNotFoundException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Fehler");
                alert.setHeaderText("Aufgabe nicht gefunden");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    public void handleMouseEvent(MouseEvent event){
        Object source = event.getSource();

        if(source == simpleView.listViewTasks && event.getEventType() == MouseEvent.MOUSE_CLICKED && event.getButton().equals(MouseButton.PRIMARY)&&event.getClickCount() == 2){
            simpleView.openNewWindow();
        }
    }

    public void handleKeyEvent(KeyEvent event){
        Object source = event.getSource();
    }

    public String[] getHabits() {
        if (useHostedTasks()) {
            return fetchHostedTasks();
        }
        return user.getAllHabits();
    }

    public String[] getInfo(){
        return simpleView.listViewTasks.getSelectionModel().getSelectedItem().toString().split(";");
    }

    public void saveData() throws IOException {
        userService.writeTaskData();
    }

    public void readData() throws IOException {
        userService.readTaskData();
    }

    public void readUserData() throws IOException {
        userService.readUserData();
    }

    public void saveUserData() throws IOException {
        userService.writeUserData();
    }

    private void persistUserStatsSafely() {
        saveUserDataAsync();
    }

    private void persistAllDataSafely() {
        runFileTask("Saving data...", false, () -> {
            saveData();
            saveUserData();
        }, null);
    }

    public User getUser() {
        return user.getUser();
    }

    public void spendGoldAndPersist(int amount) {
        user.spendGold(amount);
        invalidateStatsCache();
        persistAllDataSafely();
    }

    public UserStatsSnapshot getCachedUserStats() {
        if (sessionStore != null && sessionStore.isAuthenticated()) {
            UserProfile profile = sessionStore.apiClient().me();
            return new UserStatsSnapshot(
                    profile.username(),
                    profile.level(),
                    "Warrior",
                    profile.xp(),
                    profile.gold(),
                    profile.health()
            );
        }
        long now = System.nanoTime();
        if (cachedStats != null && (now - cachedStatsNanos) < STATS_CACHE_TTL_NANOS) {
            return cachedStats;
        }
        cachedStats = new UserStatsSnapshot(
                user.getUsername(),
                user.getLevel(),
                user.getTitle(),
                user.getExperience(),
                user.getGold(),
                user.getHealth()
        );
        cachedStatsNanos = now;
        return cachedStats;
    }

    private void invalidateStatsCache() {
        cachedStats = null;
        cachedStatsNanos = 0L;
    }

    public void loadDataAsync(Runnable onSuccess) {
        if (useHostedTasks()) {
            if (onSuccess != null) {
                onSuccess.run();
            }
            return;
        }
        runFileTask("Loading data...", true, () -> {
            readData();
            readUserData();
        }, onSuccess);
    }

    public void saveAllAsync(Runnable onSuccess) {
        if (useHostedTasks()) {
            if (onSuccess != null) {
                onSuccess.run();
            }
            return;
        }
        runFileTask("Saving data...", true, () -> {
            saveData();
            saveUserData();
        }, onSuccess);
    }

    private void saveUserDataAsync() {
        runFileTask("Saving user stats...", false, this::saveUserData, null);
    }

    private void runFileTask(String loadingMessage, boolean showLoadingIndicator, IOAction action, Runnable onSuccess) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                action.run();
                return null;
            }
        };

        task.setOnRunning(event -> {
            if (showLoadingIndicator) {
                simpleView.showLoading(loadingMessage);
            }
        });
        task.setOnSucceeded(event -> {
            if (showLoadingIndicator) {
                simpleView.hideLoading();
            }
            if (onSuccess != null) {
                onSuccess.run();
            }
            Platform.runLater(simpleView::refreshDashboardData);
        });
        task.setOnFailed(event -> {
            if (showLoadingIndicator) {
                simpleView.hideLoading();
            }
            Throwable error = task.getException();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("I/O Error");
            alert.setHeaderText("Operation failed");
            alert.setContentText(error == null ? "Unknown file operation error." : error.getMessage());
            alert.showAndWait();
        });

        Thread worker = new Thread(task, "disciplica-io-task");
        worker.setDaemon(true);
        worker.start();
    }

    @FunctionalInterface
    private interface IOAction {
        void run() throws IOException;
    }

    private boolean useHostedTasks() {
        return sessionStore != null && sessionStore.isAuthenticated();
    }

    private void createHostedTask() {
        TaskType type = toTaskType(simpleView.comboBox.getSelectionModel().getSelectedItem());
        int points = Integer.parseInt(simpleView.pointsTF.getText());
        TaskDto created = sessionStore.apiClient().createTask(new CreateTaskRequest(
                type,
                simpleView.nameTF.getText(),
                simpleView.descriptionTF.getText(),
                points,
                simpleView.categoryComboBox.getValue()
        ));
        refreshHostedTaskList();
        simpleView.refreshDashboardData();
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Task created: " + created.title());
        alert.setHeaderText("Hosted task saved");
        alert.showAndWait();
    }

    private void updateHostedTask() {
        UUID taskId = selectedHostedTaskId();
        if (taskId == null) {
            return;
        }
        TaskDto updated = sessionStore.apiClient().updateTask(taskId, new UpdateTaskRequest(
                simpleView.nameTF.getText(),
                simpleView.descriptionTF.getText(),
                Integer.parseInt(simpleView.pointsTF.getText()),
                null,
                simpleView.categoryComboBox.getValue()
        ));
        refreshHostedTaskList();
        simpleView.refreshDashboardData();
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Task updated: " + updated.title());
        alert.setHeaderText("Hosted task updated");
        alert.showAndWait();
    }

    private void deleteHostedTask() {
        UUID taskId = selectedHostedTaskId();
        if (taskId == null) {
            return;
        }
        sessionStore.apiClient().deleteTask(taskId);
        refreshHostedTaskList();
        simpleView.refreshDashboardData();
    }

    private void completeHostedTask() {
        UUID taskId = selectedHostedTaskId();
        if (taskId == null) {
            return;
        }
        TaskDto completed = sessionStore.apiClient().completeTask(taskId);
        refreshHostedTaskList();
        simpleView.refreshDashboardData();
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "You earned " + completed.points() + " XP.");
        alert.setHeaderText("Task completed");
        alert.showAndWait();
        ((Stage) simpleView.completeButton.getScene().getWindow()).close();
    }

    private UUID selectedHostedTaskId() {
        String selectedItem = simpleView.listViewTasks.getSelectionModel().getSelectedItem();
        return selectedItem == null ? null : hostedTaskIds.get(selectedItem);
    }

    private void refreshHostedTaskList() {
        simpleView.itemsObservable.setAll(fetchHostedTasks());
    }

    private String[] fetchHostedTasks() {
        hostedTaskIds.clear();
        return sessionStore.apiClient().tasks().stream()
                .map(this::toLegacyDisplay)
                .toArray(String[]::new);
    }

    private String toLegacyDisplay(TaskDto task) {
        String display = toLegacyType(task.type()) + ";" + task.title() + ";" + task.description() + ";" + task.points()
                + ";" + task.completed() + ";" + task.streak();
        hostedTaskIds.put(display, task.id());
        return display;
    }

    private TaskType toTaskType(String selectedType) {
        if ("Daily Habit".equals(selectedType)) {
            return TaskType.DAILY;
        }
        if ("Weekly Habit".equals(selectedType)) {
            return TaskType.HABIT;
        }
        return TaskType.TODO;
    }

    private String toLegacyType(TaskType type) {
        return switch (type) {
            case DAILY -> "D";
            case HABIT -> "W";
            case TODO, REWARD -> "O";
        };
    }

    public record UserStatsSnapshot(String username, int level, String title, int experience, int gold, int health) {}

    @Override
    public void handle(Event event){
        if(event instanceof ActionEvent) {
            try {
                handleActionEvent((ActionEvent) event);
            } catch (InvalidHabitException | HabitNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(event instanceof MouseEvent) {
            handleMouseEvent((MouseEvent) event);
        }
        if(event instanceof KeyEvent) {
            handleKeyEvent((KeyEvent) event);
        }
    }

    @Override
    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if(newValue == null) return;
        String[] habits = newValue.split(";");
        String typ = habits[0];
        simpleView.nameTF.setText(habits[1]);
        simpleView.descriptionTF.setText(habits[2]);
        simpleView.pointsTF.setText(habits[3]);

        if(typ.equals("D")) {
            simpleView.comboBox.getSelectionModel().select("Daily Habit");
        } else if(typ.equals("W")) {
            simpleView.comboBox.getSelectionModel().select("Weekly Habit");
        } else if(typ.equals("O")) {
            simpleView.comboBox.getSelectionModel().select("OneTimeTask");
        }
    }
}
