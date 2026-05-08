package model.domain.model;

import model.domain.contract.Completable;
import model.domain.contract.Trackable;
import model.domain.exception.InvalidHabitException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Habit implements Completable, Trackable {
    private static final Logger logger = LoggerFactory.getLogger(Habit.class);
    private final StringProperty name = new SimpleStringProperty(this, "name");
    private String description;
    private final BooleanProperty completed = new SimpleBooleanProperty(this, "completed", false);
    private final IntegerProperty streak = new SimpleIntegerProperty(this, "streak", 0);

    public Habit(String name, String description) {
        logger.debug("Creating Habit: name='{}', description='{}'", name, description);
        try {
            initializeHabit(name, description);
        } catch (InvalidHabitException invalidHabitException) {
            throw toIllegalArgument(name, invalidHabitException);
        }
    }

    private void initializeHabit(String name, String description) throws InvalidHabitException {
        setName(name);
        setDescription(description);
        logger.info("Habit created successfully: '{}'", name);
    }

    private IllegalArgumentException toIllegalArgument(String name,
            InvalidHabitException invalidHabitException) {
        logger.error("Failed to create Habit with name='{}': {}", name,
                invalidHabitException.getMessage(), invalidHabitException);
        return new IllegalArgumentException("Invalid habit data: " + invalidHabitException.getMessage(),
                invalidHabitException);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) throws InvalidHabitException {
        logger.debug("Setting name for Habit, new value='{}'", name);
        if (name == null || name.isBlank()) {
            logger.error("Habit name must not be null or blank");
            throw new InvalidHabitException("Habit name must not be null or blank");
        }
        this.name.set(name);
        logger.debug("Habit name set to '{}'", name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) throws InvalidHabitException {
        logger.debug("Setting description for Habit '{}', new value='{}'", name, description);
        if (description == null) {
            logger.error("Habit description must not be null for habit '{}'", name);
            throw new InvalidHabitException("Habit description must not be null");
        }
        this.description = description;
        logger.debug("Habit '{}' description set successfully", name);
    }

    @Override
    public int getProgress() {
        int progress = completed.get() ? 100 : 0;
        logger.debug("getProgress() for Habit '{}': {}", getName(), progress);
        return progress;
    }

    @Override
    public int getStreak() {
        logger.debug("getStreak() for Habit '{}': {}", getName(), streak.get());
        return streak.get();
    }

    public IntegerProperty streakProperty() {
        return streak;
    }

    public boolean isCompleted() {
        return completed.get();
    }

    public BooleanProperty completedProperty() {
        return completed;
    }

    @Override
    public boolean complete() {
        logger.debug("Attempting to complete Habit '{}'", getName());
        if (completed.get()) return reportAlreadyCompleted();
        markCompleted();
        return true;
    }

    private boolean reportAlreadyCompleted() {
        logger.warn("Habit already completed: {}", getName());
        return false;
    }

    private void markCompleted() {
        completed.set(true);
        streak.set(streak.get() + 1);
        logger.info("Habit completed: '{}', new streak={}", getName(), streak.get());
    }

    @Override
    public Reward getReward() {
        logger.debug("Getting reward for Habit '{}', streak={}", getName(), streak.get());
        int rewardThreshold = 10 + (streak.get() * 5);
        Reward reward = new Reward("Habit Streak", "Reward for completing " + getName(), rewardThreshold);
        logger.info("Reward generated for Habit '{}': {}", getName(), reward);
        return reward;
    }

    public void resetStreak() {
        logger.info("Resetting streak for Habit '{}', was {}", getName(), streak.get());
        streak.set(0);
        logger.debug("Streak reset complete for Habit '{}'", getName());
    }

    public void print() {
        logger.debug("print() called for Habit '{}'", getName());
        System.out.println("Habit: " + getName());
    }

    @Override
    public String toString() {
        return "Name: " + getName() + "; Description: " + description + "; isCompleted: " + completed.get()
                + "; Streak: " + streak.get();
    }
}




