package com.disciplica;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.disciplica.domain.exception.HabitNotFoundException;
import com.disciplica.domain.exception.InvalidHabitException;
import com.disciplica.domain.model.OneTimeTask;
import com.disciplica.domain.model.User;
import java.lang.reflect.Field;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    @DisplayName("Completing a task grants XP")
    void completingTaskGrantsXp() throws InvalidHabitException, HabitNotFoundException {
        User user = new User("Tester");
        OneTimeTask task = new OneTimeTask("Chore", "Clean desk", 20);

        user.addTask(task);
        user.completeTask(task);

        assertEquals(20, getPrivateInt(user, "experience"));
        assertEquals(1, getPrivateInt(user, "level"));
    }

    @Test
    @DisplayName("User levels up when XP meets threshold")
    void userLevelsUpWhenXpMeetsThreshold() throws InvalidHabitException, HabitNotFoundException {
        User user = new User("Tester");
        OneTimeTask task = new OneTimeTask("Quest", "Complete run", 60);

        user.addTask(task);
        user.completeTask(task);

        assertEquals(2, getPrivateInt(user, "level"));
        assertEquals(10, getPrivateInt(user, "experience"));
    }

    @Test
    @DisplayName("User can level up multiple times with large XP")
    void userLevelsUpMultipleTimes() throws InvalidHabitException, HabitNotFoundException {
        User user = new User("Tester");
        OneTimeTask task = new OneTimeTask("Marathon", "Long session", 160);

        user.addTask(task);
        user.completeTask(task);

        assertEquals(3, getPrivateInt(user, "level"));
        assertEquals(10, getPrivateInt(user, "experience"));
    }

    private int getPrivateInt(User user, String fieldName) {
        try {
            Field field = User.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.getInt(user);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            throw new IllegalStateException("Unable to access field: " + fieldName, ex);
        }
    }
}
