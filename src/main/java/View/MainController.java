package View;

import javafx.scene.input.MouseButton;
import model.domain.exception.HabitNotFoundException;
import model.domain.exception.InvalidHabitException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import java.util.Optional;
import model.domain.model.*;

public class MainController implements EventHandler<Event>, ChangeListener<String> {

    final User user;

    final private View simpleView;

    public MainController(View simpleView) {
        user = new User("Simon");
        this.simpleView = simpleView;
    }

    public void handleActionEvent(ActionEvent event) throws InvalidHabitException, HabitNotFoundException {
        Object source = event.getSource();

        if (source == simpleView.addButton) {
            String typ = simpleView.comboBox.getSelectionModel().getSelectedItem().toString();
            if (typ.equals("Daily Habit")) {
                try {
                    DailyHabit dailyHabit = new DailyHabit(simpleView.nameTF.getText(), simpleView.descriptionTF.getText(), Integer.parseInt(simpleView.pointsTF.getText()));
                    user.addTask(dailyHabit);
                    simpleView.itemsObservable.setAll(user.getAllHabits());
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Error parsing points. Please enter a valid integer.");
                }
            } else if (typ.equals("Weekly Habit")) {
                try {
                    WeeklyHabit weeklyHabit = new WeeklyHabit(simpleView.nameTF.getText(), simpleView.descriptionTF.getText(), Integer.parseInt(simpleView.pointsTF.getText()));
                    user.addTask(weeklyHabit);
                    simpleView.itemsObservable.setAll(user.getAllHabits());
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Error parsing points. Please enter a valid integer.");
                }
            } else if (typ.equals("OneTimeTask")) {
                try {
                    OneTimeTask oneTimeTask = new OneTimeTask(simpleView.nameTF.getText(), simpleView.descriptionTF.getText(), Integer.parseInt(simpleView.pointsTF.getText()));
                    user.addTask(oneTimeTask);
                    simpleView.itemsObservable.setAll(user.getAllHabits());
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
                } catch (HabitNotFoundException e) {
                    throw new HabitNotFoundException("Habit not found. Please check the name, description, and points.");
                }
            } else if (typ.equals("Weekly Habit")) {
                try {
                    WeeklyHabit weeklyHabit = new WeeklyHabit(simpleView.nameTF.getText(), simpleView.descriptionTF.getText(), Integer.parseInt(simpleView.pointsTF.getText()));
                    user.removeTask(weeklyHabit);
                    simpleView.itemsObservable.setAll(user.getAllHabits());
                } catch (HabitNotFoundException e) {
                    throw new HabitNotFoundException("Habit not found. Please check the name, description, and points.");
                }
            } else if (typ.equals("OneTimeTask")) {
                try {
                    OneTimeTask oneTimeTask = new OneTimeTask(simpleView.nameTF.getText(), simpleView.descriptionTF.getText(), Integer.parseInt(simpleView.pointsTF.getText()));
                    user.removeTask(oneTimeTask);
                    simpleView.itemsObservable.setAll(user.getAllHabits());
                } catch (HabitNotFoundException e) {
                    throw new HabitNotFoundException("Habit not found. Please check the name, description, and points.");
                }
            }
        }

        if (source == simpleView.changeButton) {
            try {
                String selectedItem = simpleView.listViewTasks.getSelectionModel().getSelectedItem();
                if (selectedItem == null) return;

                String[] old = selectedItem.split(";");
                String oldTypLetter = old[0];

                AbstractTask oldHabit;
                if (oldTypLetter.equals("D")) {
                    oldHabit = new DailyHabit(old[1], old[2], Integer.parseInt(old[3]));
                } else if (oldTypLetter.equals("W")) {
                    oldHabit = new WeeklyHabit(old[1], old[2], Integer.parseInt(old[3]));
                } else {
                    oldHabit = new OneTimeTask(old[1], old[2], Integer.parseInt(old[3]));
                }

                String typNew = simpleView.comboBox.getSelectionModel().getSelectedItem().toString();
                AbstractTask   newHabit;
                String name = simpleView.nameTF.getText();
                String desc = simpleView.descriptionTF.getText();
                int points = Integer.parseInt(simpleView.pointsTF.getText());

                if (typNew.equals("Daily Habit")) {
                    newHabit = new DailyHabit(name, desc, points);
                } else if (typNew.equals("Weekly Habit")) {
                    newHabit = new WeeklyHabit(name, desc, points);
                } else {
                    newHabit = new OneTimeTask(name, desc, points);
                }

                user.changeTask(oldHabit, newHabit);
                simpleView.itemsObservable.setAll(user.getAllHabits());

            } catch (InvalidHabitException | HabitNotFoundException e) {
                e.printStackTrace();
            } catch (NumberFormatException e) {
                System.out.println("Fehler: Punkte müssen eine Zahl sein!");
            }
        }
    }

    public void handleMouseEvent(MouseEvent event){
        Object source = event.getSource();

        if(source == simpleView.listViewTasks && event.getEventType() == MouseEvent.MOUSE_CLICKED && event.getButton().equals(MouseButton.PRIMARY)&&event.getClickCount() == 2>){
            simpleView.openNewWindow();
        }
    }

    public void handleKeyEvent(KeyEvent event){
        Object source = event.getSource();
    }

    public void onAddHabit() {
        Dialog<Habit> dialog = new Dialog<>();
        dialog.setTitle("Add Habit");
        dialog.setHeaderText("Enter habit details");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets()
            .add(getClass().getResource("/css/styles.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("app-root");

        TextField nameField = new TextField();
        TextField descriptionField = new TextField();
        nameField.setPromptText("Habit name");
        descriptionField.setPromptText("Habit description");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new javafx.scene.control.Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new javafx.scene.control.Label("Description:"), 0, 1);
        grid.add(descriptionField, 1, 1);
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(descriptionField, Priority.ALWAYS);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String name = nameField.getText();
                String description = descriptionField.getText();
                if (name == null || name.isBlank()) {
                    return null;
                }
                if (description == null) {
                    description = "";
                }
                return new Habit(name, description);
            }
            return null;
        });

        Optional<Habit> result = dialog.showAndWait();
        result.ifPresent(habit -> {
            simpleView.habitItemsObservable.add(habit);
            simpleView.habitListView.getSelectionModel().select(habit);
        });
    }

    public IntegerProperty levelProperty() {
        return user.levelProperty();
    }

    public IntegerProperty experienceProperty() {
        return user.experienceProperty();
    }

    public DoubleProperty completionPercentProperty() {
        return user.completionPercentProperty();
    }

    public String[] getHabits() {
        return user.getAllHabits();
    }

    public String[] getInfo(){
        return simpleView.listViewTasks.getSelectionModel().getSelectedItem().toString().split(";");
    }

    @Override
    public void handle(Event event) {
        if(event instanceof ActionEvent) {
            try {
                handleActionEvent((ActionEvent) event);
            } catch (InvalidHabitException | HabitNotFoundException e) {
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
