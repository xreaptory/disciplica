package com.disciplica.domain.model;

import com.disciplica.domain.contract.Completable;
import com.disciplica.domain.contract.Trackable;
import com.disciplica.domain.exception.InvalidHabitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Habit implements Completable, Trackable {
    private static final Logger logger = LoggerFactory.getLogger(Habit.class);
    private String name;
    private String description;
    private boolean isCompleted;
    private int streak;

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
        return name;
    }

    public void setName(String name) throws InvalidHabitException {
        logger.debug("Setting name for Habit, new value='{}'", name);
        if (name == null || name.isBlank()) {
            logger.error("Habit name must not be null or blank");
            throw new InvalidHabitException("Habit name must not be null or blank");
        }
        this.name = name;
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
        int progress = isCompleted ? 100 : 0;
        logger.debug("getProgress() for Habit '{}': {}", name, progress);
        return progress;
    }

    @Override
    public int getStreak() {
        logger.debug("getStreak() for Habit '{}': {}", name, streak);
        return streak;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    @Override
    public boolean complete() {
        logger.debug("Attempting to complete Habit '{}'", name);
        if (isCompleted) return reportAlreadyCompleted();
        markCompleted();
        return true;
    }

    private boolean reportAlreadyCompleted() {
        logger.warn("Habit already completed: {}", name);
        return false;
    }

    private void markCompleted() {
        isCompleted = true;
        streak++;
        logger.info("Habit completed: '{}', new streak={}", name, streak);
    }

    @Override
    public Reward getReward() {
        logger.debug("Getting reward for Habit '{}', streak={}", name, streak);
        int rewardThreshold = 10 + (streak * 5);
        Reward reward = new Reward("Habit Streak", "Reward for completing " + name, rewardThreshold);
        logger.info("Reward generated for Habit '{}': {}", name, reward);
        return reward;
    }

    public void resetStreak() {
        logger.info("Resetting streak for Habit '{}', was {}", name, streak);
        streak = 0;
        logger.debug("Streak reset complete for Habit '{}'", name);
    }

    public void print() {
        logger.debug("print() called for Habit '{}'", name);
        System.out.println("Habit: " + name);
    }

    @Override
    public String toString() {
        return "Name: " + name + "; Description: " + description + "; isCompleted: " + isCompleted + "; Streak: " + streak;
    }
}




