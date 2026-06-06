package model.springdata.service;

import model.domain.model.Habit;
import model.domain.model.User;

import java.util.List;

public interface HabitService {
    Habit completeHabit(Long habitId, int quality);

    List<Habit> getHabitsByUserAndFrequency(User user, String frequency);

    List<Habit> getCompletedHabits();

    List<Habit> getTopHabitsByStreak(User user);
}
