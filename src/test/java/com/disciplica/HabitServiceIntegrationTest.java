package com.disciplica;

import com.disciplica.integration.IntegrationTestBase;
import com.disciplica.testtags.IntegrationTest;
import model.domain.model.Habit;
import model.domain.model.User;
import model.springdata.repository.CompletionRepository;
import model.springdata.repository.HabitRepository;
import model.springdata.repository.UserSpringRepository;
import model.springdata.service.HabitService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@IntegrationTest
class HabitServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private HabitService habitService;
    @Autowired
    private UserSpringRepository userRepository;
    @Autowired
    private HabitRepository habitRepository;
    @Autowired
    private CompletionRepository completionRepository;

    @Test
    @DisplayName("Full user registration flow persists and can be queried by username")
    void fullUserRegistrationFlow() {
        User user = new User("IntegrationUser");
        User saved = userRepository.save(user);

        assertNotNull(saved.getId());

        Optional<User> loaded = userRepository.findByUsername("IntegrationUser");
        assertTrue(loaded.isPresent());
        assertEquals("IntegrationUser", loaded.get().getUsername());
        assertEquals(1, loaded.get().getLevel());
    }

    @Test
    @DisplayName("Complete habit applies side effects: completion row, streak, XP, and gold")
    void completeHabitWithAllSideEffects() {
        User user = new User("QuestUser");
        Habit habit = new Habit("Run", "Morning run");
        user.addHabit(habit);
        userRepository.save(user);

        Long habitId = habit.getId();
        Long userId = user.getId();
        assertNotNull(habitId);
        assertNotNull(userId);
        assertEquals(0, completionRepository.count());

        habitService.completeHabit(habitId, 1);

        Habit updatedHabit = habitRepository.findById(habitId).orElseThrow();
        User updatedUser = userRepository.findById(userId).orElseThrow();

        assertTrue(updatedHabit.isCompleted());
        assertEquals(1, updatedHabit.getStreak());
        assertEquals(1, completionRepository.count());
        assertFalse(updatedUser.getExperience() < 0);
        assertFalse(updatedUser.getGold() < 0);
        assertTrue(updatedUser.getExperience() > 0);
    }
}
