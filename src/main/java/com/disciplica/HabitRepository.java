package com.disciplica;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * In-memory, type-safe repository for {@link Habit} entities.
 * Implements {@link Repository Repository&lt;Habit&gt;}.
 */
public class HabitRepository implements Repository<Habit> {

    private static final Logger logger = LoggerFactory.getLogger(HabitRepository.class);
    private final List<Habit> habits = new ArrayList<>();

    @Override
    public void save(Habit entity) throws InvalidHabitException {
        logger.debug("HabitRepository.save() called for '{}'", entity);
        if (entity == null) {
            logger.error("Cannot save a null Habit");
            throw new InvalidHabitException("Cannot save a null Habit");
        }
        // Update if exists, otherwise add
        Optional<Habit> existing = findByName(entity.getName());
        if (existing.isPresent()) {
            habits.remove(existing.get());
            logger.debug("Replacing existing Habit '{}'", entity.getName());
        }
        habits.add(entity);
        logger.info("Habit '{}' saved to repository ({} total)", entity.getName(), habits.size());
    }

    @Override
    public Optional<Habit> findByName(String name) {
        logger.debug("HabitRepository.findByName() called, name='{}'", name);
        return habits.stream()
                .filter(h -> h.getName().equals(name))
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

