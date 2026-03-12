package com.disciplica;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * In-memory, type-safe repository for {@link Reward} entities.
 * Implements {@link Repository Repository&lt;Reward&gt;}.
 */
public class RewardRepository implements Repository<Reward> {

    private static final Logger logger = LoggerFactory.getLogger(RewardRepository.class);
    private final List<Reward> rewards = new ArrayList<>();

    @Override
    public void save(Reward entity) throws InvalidHabitException {
        logger.debug("RewardRepository.save() called for '{}'", entity);
        if (entity == null) {
            logger.error("Cannot save a null Reward");
            throw new InvalidHabitException("Cannot save a null Reward");
        }
        // Update if exists, otherwise add
        Optional<Reward> existing = findByName(entity.getName());
        if (existing.isPresent()) {
            rewards.remove(existing.get());
            logger.debug("Replacing existing Reward '{}'", entity.getName());
        }
        rewards.add(entity);
        logger.info("Reward '{}' saved to repository ({} total)", entity.getName(), rewards.size());
    }

    @Override
    public Optional<Reward> findByName(String name) {
        logger.debug("RewardRepository.findByName() called, name='{}'", name);
        return rewards.stream()
                .filter(r -> r.getName().equals(name))
                .findFirst();
    }

    @Override
    public List<Reward> findAll() {
        logger.debug("RewardRepository.findAll() called, returning {} reward(s)", rewards.size());
        return new ArrayList<>(rewards);
    }

    @Override
    public void delete(String name) throws HabitNotFoundException {
        logger.debug("RewardRepository.delete() called, name='{}'", name);
        Reward reward = findByName(name)
                .orElseThrow(() -> {
                    logger.error("Reward '{}' not found for deletion", name);
                    return new HabitNotFoundException("Reward not found: " + name);
                });
        rewards.remove(reward);
        logger.info("Reward '{}' deleted from repository ({} remaining)", name, rewards.size());
    }
}

