package com.disciplica;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import model.domain.model.Habit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HabitTest {

    @Test
    @DisplayName("Create habit sets fields and defaults")
    void createHabitSetsFieldsAndDefaults() {
        Habit habit = new Habit("Read", "Read 10 pages");

        assertEquals("Read", habit.getName());
        assertEquals("Read 10 pages", habit.getDescription());
        assertFalse(habit.isCompleted());
        assertEquals(0, habit.getStreak());
        assertEquals(0, habit.getProgress());
    }

    @Test
    @DisplayName("Complete marks habit completed and increments streak")
    void completeMarksCompletedAndIncrementsStreak() {
        Habit habit = new Habit("Hydrate", "Drink water");

        assertTrue(habit.complete());
        assertTrue(habit.isCompleted());
        assertEquals(1, habit.getStreak());
        assertEquals(100, habit.getProgress());
    }

    @Test
    @DisplayName("Completing twice returns false and keeps streak")
    void completingTwiceReturnsFalseAndKeepsStreak() {
        Habit habit = new Habit("Stretch", "Morning stretch");

        assertTrue(habit.complete());
        assertFalse(habit.complete());
        assertEquals(1, habit.getStreak());
    }

    @Test
    @DisplayName("Reset streak sets streak back to zero")
    void resetStreakSetsStreakBackToZero() {
        Habit habit = new Habit("Journal", "Daily reflection");

        assertTrue(habit.complete());
        habit.resetStreak();

        assertEquals(0, habit.getStreak());
    }

    @Test
    @DisplayName("Progress reflects completion state")
    void progressReflectsCompletionState() {
        Habit habit = new Habit("Meditate", "10 minutes");

        assertEquals(0, habit.getProgress());
        habit.complete();
        assertEquals(100, habit.getProgress());
    }
}
