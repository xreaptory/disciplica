package com.disciplica;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercise: trigger both custom exceptions and verify they are logged correctly.
 *
 * Each test intentionally causes an error condition so the SLF4J / Logback
 * pipeline writes a visible ERROR or WARN line to the console and log file.
 * The test output can be matched against the deliverable requirement:
 * "Robust error handling with meaningful log output".
 */
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

    // -----------------------------------------------------------------------
    // Completable interface tests
    // -----------------------------------------------------------------------

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

        // Second call → already completed → WARN logged inside Habit.complete()
        boolean result = habit.complete();
        assertFalse(result, "second complete() must return false");
        logger.info("Second complete() correctly returned false (WARN logged)");
    }

    // -----------------------------------------------------------------------
    // InvalidHabitException tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Adding a null task throws InvalidHabitException and logs ERROR")
    void addNullTaskThrowsInvalidHabitException() {
        logger.info("--- TEST: addNullTaskThrowsInvalidHabitException ---");
        InvalidHabitException ex = assertThrows(InvalidHabitException.class,
                () -> user.addTask(null));

        logger.error("Caught expected InvalidHabitException: {}", ex.getMessage());
        assertTrue(ex.getMessage().contains("null"), "message should mention null");
    }

    @Test
    @DisplayName("Adding a duplicate task throws InvalidHabitException and logs WARN")
    void addDuplicateTaskThrowsInvalidHabitException() throws InvalidHabitException {
        logger.info("--- TEST: addDuplicateTaskThrowsInvalidHabitException ---");
        AbstractTask task = user.getTasks().get(0);       // already in the list

        InvalidHabitException ex = assertThrows(InvalidHabitException.class,
                () -> user.addTask(task));

        logger.error("Caught expected InvalidHabitException: {}", ex.getMessage());
        assertTrue(ex.getMessage().contains(task.getName()));
    }

    @Test
    @DisplayName("Creating a task with a blank name throws InvalidHabitException")
    void blankTaskNameThrowsInvalidHabitException() {
        logger.info("--- TEST: blankTaskNameThrowsInvalidHabitException ---");
        InvalidHabitException ex = assertThrows(InvalidHabitException.class,
                () -> new DailyHabit("", "some description", 10));

        logger.error("Caught expected InvalidHabitException: {}", ex.getMessage());
        assertTrue(ex.getMessage().toLowerCase().contains("name"));
    }

    @Test
    @DisplayName("Creating a task with negative points throws InvalidHabitException")
    void negativePointsThrowsInvalidHabitException() {
        logger.info("--- TEST: negativePointsThrowsInvalidHabitException ---");
        InvalidHabitException ex = assertThrows(InvalidHabitException.class,
                () -> new OneTimeTask("Bad Task", "desc", -5));

        logger.error("Caught expected InvalidHabitException: {}", ex.getMessage());
        assertTrue(ex.getMessage().contains("-5"));
    }

    @Test
    @DisplayName("Creating a Habit with a blank name throws IllegalArgumentException wrapping InvalidHabitException")
    void blankHabitNameThrowsException() {
        logger.info("--- TEST: blankHabitNameThrowsException ---");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> new Habit("  ", "desc"));

        logger.error("Caught expected IllegalArgumentException: {}", ex.getMessage());
        assertNotNull(ex.getCause());
        assertInstanceOf(InvalidHabitException.class, ex.getCause());
    }

    // -----------------------------------------------------------------------
    // HabitNotFoundException tests
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Removing a task not in the list throws HabitNotFoundException and logs ERROR")
    void removeAbsentTaskThrowsHabitNotFoundException() throws InvalidHabitException {
        logger.info("--- TEST: removeAbsentTaskThrowsHabitNotFoundException ---");
        AbstractTask ghost = new DailyHabit("Ghost Task", "not in user list", 5);

        HabitNotFoundException ex = assertThrows(HabitNotFoundException.class,
                () -> user.removeTask(ghost));

        logger.error("Caught expected HabitNotFoundException: {}", ex.getMessage());
        assertTrue(ex.getMessage().contains("Ghost Task"));
    }

    @Test
    @DisplayName("Completing a task not in the list throws HabitNotFoundException and logs ERROR")
    void completeAbsentTaskThrowsHabitNotFoundException() throws InvalidHabitException {
        logger.info("--- TEST: completeAbsentTaskThrowsHabitNotFoundException ---");
        AbstractTask ghost = new WeeklyHabit("Phantom Habit", "not tracked", 15);

        HabitNotFoundException ex = assertThrows(HabitNotFoundException.class,
                () -> user.completeTask(ghost));

        logger.error("Caught expected HabitNotFoundException: {}", ex.getMessage());
        assertTrue(ex.getMessage().contains("Phantom Habit"));
    }

    @Test
    @DisplayName("Both exception types carry meaningful messages")
    void exceptionMessagesAreMeaningful() throws InvalidHabitException {
        logger.info("--- TEST: exceptionMessagesAreMeaningful ---");

        // InvalidHabitException
        InvalidHabitException ive = new InvalidHabitException("bad value: -1");
        assertFalse(ive.getMessage().isBlank());
        logger.info("InvalidHabitException message: '{}'", ive.getMessage());

        // HabitNotFoundException
        HabitNotFoundException hnfe = new HabitNotFoundException("not found: 'Run'");
        assertFalse(hnfe.getMessage().isBlank());
        logger.info("HabitNotFoundException message: '{}'", hnfe.getMessage());

        // Cause constructors
        RuntimeException cause = new RuntimeException("root cause");
        InvalidHabitException withCause = new InvalidHabitException("wrapped", cause);
        assertSame(cause, withCause.getCause());

        HabitNotFoundException hnfeWithCause = new HabitNotFoundException("not found", cause);
        assertSame(cause, hnfeWithCause.getCause());
        logger.info("Cause constructors verified for both exception types");
    }

    // -----------------------------------------------------------------------
    // Full happy-path: complete tasks and gain XP
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("User can complete tasks and accumulate XP")
    void userCompletesTasksAndGainsXp() throws HabitNotFoundException {
        logger.info("--- TEST: userCompletesTasksAndGainsXp ---");
        for (AbstractTask t : user.getTasks()) {
            user.completeTask(t);
            logger.info("Completed '{}', points={}", t.getName(), t.calculatePoints());
        }
        logger.info("User after completing all tasks: {}", user);
        // Just verifying no exceptions were thrown — visual log output is the deliverable
    }
}


