package model.domain.model;

import model.domain.contract.Trackable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Reward implements Trackable {
    private static final Logger logger = LoggerFactory.getLogger(Reward.class);
    private String name;
    private String description;
    private int pointsRequired;

    public Reward(String name, String description, int pointsRequired) {
        this.name = name;
        this.description = description;
        this.pointsRequired = pointsRequired;
        logger.debug("Reward created: name='{}', pointsRequired={}", name, pointsRequired);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        logger.debug("Reward name changed from '{}' to '{}'", this.name, name);
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        logger.debug("Reward '{}' description updated", this.name);
        this.description = description;
    }

    public int getPointsRequired() {
        return pointsRequired;
    }

    public void setPointsRequired(int pointsRequired) {
        logger.debug("Reward '{}' pointsRequired changed from {} to {}", name, this.pointsRequired, pointsRequired);
        this.pointsRequired = pointsRequired;
    }

    @Override
    public int getProgress() {
        return 0;
    }

    @Override
    public int getStreak() {
        return 0;
    }

    @Override
    public String toString() {
        return name + " (" + pointsRequired + " pts) - " + description;
    }
}


