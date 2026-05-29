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

import java.io.IOException;

public class MainController implements EventHandler<Event>, ChangeListener<String> {

    final User user;
    private final UserService userService;

    final private View simpleView;

    public MainController(View simpleView, UserService userService) {
        this.userService = userService;
        user = userService.getUser();
        this.simpleView = simpleView;
    }

    public void handleActionEvent(ActionEvent event) throws InvalidHabitException, HabitNotFoundException, IOException {
        Object source = event.getSource();

        if(source == simpleView.habitsBTN){
            simpleView.openHabitMenu();
        }

        if(source == simpleView.statsBTN){
            simpleView.openStats();
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
            String typ = simpleView.comboBox.getSelectionModel().getSelectedItem().toString();
            if (typ.equals("Daily Habit")) {
                try {
                    DailyHabit dailyHabit = new DailyHabit(simpleView.nameTF.getText(), simpleView.descriptionTF.getText(), Integer.parseInt(simpleView.pointsTF.getText()));
                    user.addTask(dailyHabit);
                    simpleView.itemsObservable.setAll(user.getAllHabits());
                    simpleView.refreshDashboardData();
                    persistUserStatsSafely();
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Error parsing points. Please enter a valid integer.");
                }
            } else if (typ.equals("Weekly Habit")) {
                try {
                    WeeklyHabit weeklyHabit = new WeeklyHabit(simpleView.nameTF.getText(), simpleView.descriptionTF.getText(), Integer.parseInt(simpleView.pointsTF.getText()));
                    user.addTask(weeklyHabit);
                    simpleView.itemsObservable.setAll(user.getAllHabits());
                    simpleView.refreshDashboardData();
                    persistUserStatsSafely();
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Error parsing points. Please enter a valid integer.");
                }
            } else if (typ.equals("OneTimeTask")) {
                try {
                    OneTimeTask oneTimeTask = new OneTimeTask(simpleView.nameTF.getText(), simpleView.descriptionTF.getText(), Integer.parseInt(simpleView.pointsTF.getText()));
                    user.addTask(oneTimeTask);
                    simpleView.itemsObservable.setAll(user.getAllHabits());
                    simpleView.refreshDashboardData();
                    persistUserStatsSafely();
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Error parsing points. Please enter a valid integer.");
                }
            }
        }

        if (source == simpleView.removeButton) {
            String typ = simpleView.comboBox.getSelectionModel().getSelectedItem().toString();
            if (typ.equals("Daily Habit")) {
                try {
                    DailyHabit dailyHabit = new DailyHabit(simpleView.nameTF.getText(), simpleView.descriptionTF.getText(), Integer.parseInt(simpleView.pointsTF.getText()));
                    user.removeTask(dailyHabit);
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
                    simpleView.itemsObservable.setAll(user.getAllHabits());
                    simpleView.refreshDashboardData();
                    persistUserStatsSafely();
                } catch (HabitNotFoundException e) {
                    throw new HabitNotFoundException("Habit not found. Please check the name, description, and points.");
                }
            }
        }

        if (source == simpleView.changeButton) {
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
            try{
                user.printTasks();

                String searchedName = simpleView.nameField.getText();
                String typ = simpleView.typeField.getText();
                AbstractTask task = user.getTaskName(searchedName);


                if(task == null) {
                    throw new HabitNotFoundException("Habit with name '" + searchedName + "' not found. Please check the name.");
                }

                user.completeTask(task);
                if(typ.equals("OneTimeTask")) {
                    user.removeTask(task);
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

    public User getUser() {
        return user.getUser();
    }

    public void loadDataAsync(Runnable onSuccess) {
        runFileTask("Loading data...", true, () -> {
            readData();
            readUserData();
        }, onSuccess);
    }

    public void saveAllAsync(Runnable onSuccess) {
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
