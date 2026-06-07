package com.disciplica;

import com.disciplica.testtags.UnitTest;
import model.domain.model.Habit;
import model.domain.model.User;
import model.springdata.repository.CompletionRepository;
import model.springdata.repository.HabitRepository;
import model.springdata.repository.UserSpringRepository;
import model.springdata.service.HabitServiceException;
import model.springdata.service.HabitServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@UnitTest
class HabitServiceImplMockitoTest {

    @Mock
    HabitRepository habitRepository;
    @Mock
    UserSpringRepository userRepository;
    @Mock
    CompletionRepository completionRepository;

    @InjectMocks
    HabitServiceImpl habitService;

    @Test
    @DisplayName("completeHabit: stubs findById, saves entities, and returns completed habit")
    void completeHabitSuccess() {
        User user = new User("MockUser");
        Habit habit = new Habit("Run", "Morning run");
        habit.setUser(user);

        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));

        Habit result = habitService.completeHabit(1L, 1);

        assertTrue(result.isCompleted());
        assertTrue(user.getExperience() > 0);
        assertTrue(user.getGold() >= 0);
        verify(habitRepository).findById(1L);
        verify(completionRepository).save(any());
        verify(userRepository).save(any());
        verify(habitRepository).save(any());
    }

    @Test
    @DisplayName("completeHabit: throws service exception when habit is missing")
    void completeHabitHabitNotFound() {
        when(habitRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(HabitServiceException.class, () -> habitService.completeHabit(1L, 1));
        verify(habitRepository).findById(1L);
    }

    @Test
    @DisplayName("completeHabit: doThrow on repository.save propagates error")
    void completeHabitSaveFails() {
        User user = new User("MockUser");
        Habit habit = new Habit("Read", "Read docs");
        habit.setUser(user);

        when(habitRepository.findById(1L)).thenReturn(Optional.of(habit));
        doThrow(new RuntimeException("DB write failed")).when(habitRepository).save(any(Habit.class));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> habitService.completeHabit(1L, 1));
        assertEquals("DB write failed", ex.getMessage());

        verify(habitRepository).findById(1L);
        verify(completionRepository).save(any());
        verify(habitRepository).save(any(Habit.class));
    }
}
