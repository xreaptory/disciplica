package model.persistence;

import model.domain.model.Habit;
import model.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface HabitRepository {
    void save(Habit habit);

    Optional<Habit> findById(Long id);

    List<Habit> findByUser(User user);

    void update(Habit habit);

    void delete(Long id);

    List<Habit> findAll();

    void completeHabit(Long habitId, int quality, int xpGain, int goldGain);
}
