package com.disciplica.infrastructure.persistence;

import com.disciplica.domain.exception.HabitNotFoundException;
import com.disciplica.domain.exception.InvalidHabitException;
import com.disciplica.domain.model.Reward;
import com.disciplica.domain.repository.Repository;
import org.slf4j.*;
import java.util.*;

public class RewardRepository implements Repository<Reward> {

    private static final Logger logger = LoggerFactory.getLogger(RewardRepository.class);
    private final List<Reward> rewards = new ArrayList<>();

    @Override
    public void save(Reward entity) throws InvalidHabitException {
        logger.debug("RewardRepository.save() called for '{}'", entity);
        validateEntity(entity);
        replaceExisting(entity);
        rewards.add(entity);
        logger.info("Reward '{}' saved to repository ({} total)", entity.getName(), rewards.size());
    }

    private void validateEntity(Reward entity) throws InvalidHabitException {
        if (entity == null) {
            logger.error("Cannot save a null Reward");
            throw new InvalidHabitException("Cannot save a null Reward");
        }
    }

    private void replaceExisting(Reward entity) {
        Optional<Reward> existingReward = findByName(entity.getName());
        if (existingReward.isPresent()) {
            rewards.remove(existingReward.get());
            logger.debug("Replacing existing Reward '{}'", entity.getName());
        }
    }

    @Override
    public Optional<Reward> findByName(String name) {
        logger.debug("RewardRepository.findByName() called, name='{}'", name);
        return rewards.stream()
            .filter(reward -> reward.getName().equals(name))
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



