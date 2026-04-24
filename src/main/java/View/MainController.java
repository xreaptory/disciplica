package View;

import com.disciplica.domain.exception.HabitNotFoundException;
import com.disciplica.domain.exception.InvalidHabitException;
import com.disciplica.domain.model.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;

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
    }

    public void handleMouseEvent(MouseEvent event){
        Object source = event.getSource();
    }

    public void handleKeyEvent(KeyEvent event){
        Object source = event.getSource();
    }

    public String[] getHabits() {
        return user.getAllHabits();
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
        simpleView.nameTF.setText(habits[0]);
        simpleView.descriptionTF.setText(habits[1]);
        simpleView.pointsTF.setText(habits[2]);
    }
}
