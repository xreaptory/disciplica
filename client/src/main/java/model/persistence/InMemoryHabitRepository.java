package model.persistence;

import model.domain.model.Habit;
import model.domain.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InMemoryHabitRepository implements HabitRepository {
    private final List<Habit> habits = new ArrayList<>();

    @Override
    public void save(Habit habit) {
        habits.add(habit);
    }

    @Override
    public Optional<Habit> findById(Long id) {
        if (id == null || id < 0 || id >= habits.size()) {
            return Optional.empty();
        }
        return Optional.of(habits.get(id.intValue()));
    }

    @Override
    public List<Habit> findByUser(User user) {
        return new ArrayList<>(habits);
    }

    @Override
    public void update(Habit habit) {
        int index = habits.indexOf(habit);
        if (index >= 0) {
            habits.set(index, habit);
        }
    }

    @Override
    public void delete(Long id) {
        findById(id).ifPresent(habits::remove);
    }

    @Override
    public List<Habit> findAll() {
        return new ArrayList<>(habits);
    }

    @Override
    public void completeHabit(Long habitId, int quality, int xpGain, int goldGain) {
        Habit habit = findById(habitId).orElseThrow(() -> new DatabaseException("Habit not found"));
        habit.complete();
    }
}
