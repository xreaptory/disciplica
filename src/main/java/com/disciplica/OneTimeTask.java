package com.disciplica;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OneTimeTask extends AbstractTask {
    private static final Logger logger = LoggerFactory.getLogger(OneTimeTask.class);

    public OneTimeTask(String name, String description, int points) throws InvalidHabitException {
        super(name, description, points);
        logger.debug("OneTimeTask created: name='{}', points={}", name, points);
    }

    @Override
    public int calculatePoints() {
        int total = super.getPoints();
        logger.debug("Calculated points for OneTimeTask '{}': {}", getName(), total);
        return total;
    }
}
