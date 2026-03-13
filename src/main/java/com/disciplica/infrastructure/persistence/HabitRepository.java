package com.disciplica.infrastructure.persistence;

import com.disciplica.domain.exception.HabitNotFoundException;
import com.disciplica.domain.exception.InvalidHabitException;
import com.disciplica.domain.model.Habit;
import com.disciplica.domain.repository.Repository;
import org.slf4j.*;
import java.util.*;

public class HabitRepository implements Repository<Habit> {

    private static final Logger logger = LoggerFactory.getLogger(HabitRepository.class);
    private final List<Habit> habits = new ArrayList<>();

    @Override
    public void save(Habit entity) throws InvalidHabitException {
        logger.debug("HabitRepository.save() called for '{}'", entity);
        validateEntity(entity);
        replaceExisting(entity);
        habits.add(entity);
        logger.info("Habit '{}' saved to repository ({} total)", entity.getName(), habits.size());
    }

    private void validateEntity(Habit entity) throws InvalidHabitException {
        if (entity == null) {
            logger.error("Cannot save a null Habit");
            throw new InvalidHabitException("Cannot save a null Habit");
        }
    }

    private void replaceExisting(Habit entity) {
        Optional<Habit> existingHabit = findByName(entity.getName());
        if (existingHabit.isPresent()) {
            habits.remove(existingHabit.get());
            logger.debug("Replacing existing Habit '{}'", entity.getName());
        }
    }

    @Override
    public Optional<Habit> findByName(String name) {
        logger.debug("HabitRepository.findByName() called, name='{}'", name);
        return habits.stream()
            .filter(habit -> habit.getName().equals(name))
                .findFirst();
    }

    @Override
    public List<Habit> findAll() {
        logger.debug("HabitRepository.findAll() called, returning {} habit(s)", habits.size());
        return new ArrayList<>(habits);
    }

    @Override
    public void delete(String name) throws HabitNotFoundException {
        logger.debug("HabitRepository.delete() called, name='{}'", name);
        Habit habit = findByName(name)
                .orElseThrow(() -> {
                    logger.error("Habit '{}' not found for deletion", name);
                    return new HabitNotFoundException("Habit not found: " + name);
                });
        habits.remove(habit);
        logger.info("Habit '{}' deleted from repository ({} remaining)", name, habits.size());
    }
}



