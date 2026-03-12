package com.disciplica;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTask implements Trackable {
    private static final Logger logger = LoggerFactory.getLogger(AbstractTask.class);
    private final String name;
    private final String description;
    private boolean isCompleted;
    private final int points; // Base points

    public AbstractTask(String name, String description, int points) throws InvalidHabitException {
        logger.debug("AbstractTask constructor: name='{}', points={}", name, points);
        if (name == null || name.isBlank()) {
            logger.error("Task name must not be null or blank");
            throw new InvalidHabitException("Task name must not be null or blank");
        }
        if (description == null) {
            logger.error("Task description must not be null for task '{}'", name);
            throw new InvalidHabitException("Task description must not be null");
        }
        if (points < 0) {
            logger.error("Task points must be non-negative, got {} for task '{}'", points, name);
            throw new InvalidHabitException("Task points must be non-negative, got: " + points);
        }
        this.name = name;
        this.description = description;
        this.points = points;
        this.isCompleted = false;
        logger.info("Task created: '{}' ({}), points={}", name, this.getClass().getSimpleName(), points);
    }

    @Override
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public int getPoints() {
        return points;
    }

    public boolean complete() {
        logger.debug("complete() called on '{}' ({})", name, this.getClass().getSimpleName());
        if (!isCompleted) {
            isCompleted = true;
            logger.info("Task completed: '{}' ({})", name, this.getClass().getSimpleName());
            return true;
        }
        logger.warn("Attempted to complete already-completed task: '{}' ({})", name, this.getClass().getSimpleName());
        return false;
    }

    public abstract int calculatePoints();

    @Override
    public int getProgress() {
        return isCompleted ? 100 : 0;
    }

    /** Returns the current streak count. Subclasses may override. */
    @Override
    public int getStreak() {
        return 0;
    }

    @Override
    public String toString() {
        return "Name: " + name + " (" + this.getClass().getSimpleName() + ") - " + (isCompleted ? "[DONE]" : "[ ]");
    }
}
