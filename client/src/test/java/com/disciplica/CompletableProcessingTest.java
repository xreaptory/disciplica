package com.disciplica;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import model.domain.exception.HabitNotFoundException;
import model.domain.exception.InvalidHabitException;
import model.domain.model.AbstractTask;
import model.domain.model.DailyHabit;
import model.domain.model.Habit;
import model.domain.model.OneTimeTask;
import model.domain.model.Reward;
import model.domain.model.User;
import model.domain.model.WeeklyHabit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CompletableProcessingTest {

    private static final Logger logger = LoggerFactory.getLogger(CompletableProcessingTest.class);
    private User user;

    @BeforeEach
    void setUp() throws InvalidHabitException {
        logger.info("=== Test setUp: creating fresh User ===");
        user = new User("TestUser");
        user.addTask(new DailyHabit("Morning Run", "Run 5 km", 20));
        user.addTask(new WeeklyHabit("Deep Clean", "Clean entire flat", 30));
        user.addTask(new OneTimeTask("Buy Gear", "Sports shoes", 10));
    }

    @Test
    @DisplayName("Habit completes and returns a non-null reward")
    void habitCompletesAndReturnsReward() {
        logger.info("--- TEST: habitCompletesAndReturnsReward ---");
        Habit habit = new Habit("Hydrate", "Drink water");

        assertTrue(habit.complete(), "first complete() call must return true");

        Reward reward = habit.getReward();
        assertNotNull(reward);
        assertNotNull(reward.getName());
        assertTrue(reward.getPointsRequired() > 0);
        logger.info("Reward received: {}", reward);
    }

    @Test
    @DisplayName("Completing a Habit a second time returns false and logs WARN")
    void habitAlreadyCompletedReturnsFalse() {
        logger.info("--- TEST: habitAlreadyCompletedReturnsFalse ---");
        Habit habit = new Habit("Meditate", "10-minute session");
        assertTrue(habit.complete());

        boolean secondCompletionResult = habit.complete();
        assertFalse(secondCompletionResult, "second complete() must return false");
        logger.info("Second complete() correctly returned false (WARN logged)");
    }

    @Test
    @DisplayName("Adding a null task throws InvalidHabitException and logs ERROR")
    void addNullTaskThrowsInvalidHabitException() {
        logger.info("--- TEST: addNullTaskThrowsInvalidHabitException ---");
        InvalidHabitException thrownException = assertThrows(InvalidHabitException.class,
                () -> user.addTask(null));

        logger.error("Caught expected InvalidHabitException: {}", thrownException.getMessage());
        assertTrue(thrownException.getMessage().contains("null"), "message should mention null");
    }

    @Test
    @DisplayName("Adding a duplicate task throws InvalidHabitException and logs WARN")
    void addDuplicateTaskThrowsInvalidHabitException() throws InvalidHabitException {
        logger.info("--- TEST: addDuplicateTaskThrowsInvalidHabitException ---");
        AbstractTask existingTask = user.getTasks().get(0);

        InvalidHabitException thrownException = assertThrows(InvalidHabitException.class,
                () -> user.addTask(existingTask));

        logger.error("Caught expected InvalidHabitException: {}", thrownException.getMessage());
        assertTrue(thrownException.getMessage().contains(existingTask.getName()));
    }

    @Test
    @DisplayName("Creating a task with a blank name throws InvalidHabitException")
    void blankTaskNameThrowsInvalidHabitException() {
        logger.info("--- TEST: blankTaskNameThrowsInvalidHabitException ---");
        InvalidHabitException thrownException = assertThrows(InvalidHabitException.class,
                () -> new DailyHabit("", "some description", 10));

        logger.error("Caught expected InvalidHabitException: {}", thrownException.getMessage());
        assertTrue(thrownException.getMessage().toLowerCase().contains("name"));
    }

    @Test
    @DisplayName("Creating a task with negative points throws InvalidHabitException")
    void negativePointsThrowsInvalidHabitException() {
        logger.info("--- TEST: negativePointsThrowsInvalidHabitException ---");
        InvalidHabitException thrownException = assertThrows(InvalidHabitException.class,
                () -> new OneTimeTask("Bad Task", "desc", -5));

        logger.error("Caught expected InvalidHabitException: {}", thrownException.getMessage());
        assertTrue(thrownException.getMessage().contains("-5"));
    }

    @Test
    @DisplayName("Creating a Habit with a blank name throws IllegalArgumentException wrapping InvalidHabitException")
    void blankHabitNameThrowsException() {
        logger.info("--- TEST: blankHabitNameThrowsException ---");
        IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class,
                () -> new Habit("  ", "desc"));

        logger.error("Caught expected IllegalArgumentException: {}", thrownException.getMessage());
        assertNotNull(thrownException.getCause());
        assertInstanceOf(InvalidHabitException.class, thrownException.getCause());
    }

    @Test
    @DisplayName("Removing a task not in the list throws HabitNotFoundException and logs ERROR")
    void removeAbsentTaskThrowsHabitNotFoundException() throws InvalidHabitException {
        logger.info("--- TEST: removeAbsentTaskThrowsHabitNotFoundException ---");
        AbstractTask missingTask = new DailyHabit("Ghost Task", "not in user list", 5);

        HabitNotFoundException thrownException = assertThrows(HabitNotFoundException.class,
                () -> user.removeTask(missingTask));

        logger.error("Caught expected HabitNotFoundException: {}", thrownException.getMessage());
        assertTrue(thrownException.getMessage().contains("Ghost Task"));
    }

    @Test
    @DisplayName("Completing a task not in the list throws HabitNotFoundException and logs ERROR")
    void completeAbsentTaskThrowsHabitNotFoundException() throws InvalidHabitException {
        logger.info("--- TEST: completeAbsentTaskThrowsHabitNotFoundException ---");
        AbstractTask missingTask = new WeeklyHabit("Phantom Habit", "not tracked", 15);

        HabitNotFoundException thrownException = assertThrows(HabitNotFoundException.class,
                () -> user.completeTask(missingTask));

        logger.error("Caught expected HabitNotFoundException: {}", thrownException.getMessage());
        assertTrue(thrownException.getMessage().contains("Phantom Habit"));
    }

    @Test
    @DisplayName("Both exception types carry meaningful messages")
    void exceptionMessagesAreMeaningful() throws InvalidHabitException {
        logger.info("--- TEST: exceptionMessagesAreMeaningful ---");

        InvalidHabitException invalidHabitException = new InvalidHabitException("bad value: -1");
        assertFalse(invalidHabitException.getMessage().isBlank());
        logger.info("InvalidHabitException message: '{}'", invalidHabitException.getMessage());

        HabitNotFoundException habitNotFoundException = new HabitNotFoundException("not found: 'Run'");
        assertFalse(habitNotFoundException.getMessage().isBlank());
        logger.info("HabitNotFoundException message: '{}'", habitNotFoundException.getMessage());

        RuntimeException rootCause = new RuntimeException("root cause");
        InvalidHabitException invalidHabitWithCause = new InvalidHabitException("wrapped", rootCause);
        assertSame(rootCause, invalidHabitWithCause.getCause());

        HabitNotFoundException habitNotFoundWithCause = new HabitNotFoundException("not found", rootCause);
        assertSame(rootCause, habitNotFoundWithCause.getCause());
        logger.info("Cause constructors verified for both exception types");
    }

    @Test
    @DisplayName("User can complete tasks and accumulate XP")
    void userCompletesTasksAndGainsXp() throws HabitNotFoundException {
        logger.info("--- TEST: userCompletesTasksAndGainsXp ---");
        for (AbstractTask task : user.getTasks()) {
            user.completeTask(task);
            logger.info("Completed '{}', points={}", task.getName(), task.calculatePoints());
        }
        logger.info("User after completing all tasks: {}", user);
    }
}
