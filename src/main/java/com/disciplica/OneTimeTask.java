package com.disciplica;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OneTimeTask extends AbstractTask {
    private static final Logger logger = LoggerFactory.getLogger(OneTimeTask.class);

    @JsonCreator
    public OneTimeTask(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("points") int points,
            @JsonProperty("completed") boolean completed) throws InvalidHabitException {
        super(name, description, points);
        if (completed) {
            super.complete(); // Restore completed state
        }
        logger.debug("OneTimeTask created: name='{}', points={}, completed={}", name, points, completed);
    }

    // Convenience constructor for creating new tasks
    public OneTimeTask(String name, String description, int points) throws InvalidHabitException {
        this(name, description, points, false);
    }

    @Override
    public int calculatePoints() {
        int total = super.getPoints();
        logger.debug("Calculated points for OneTimeTask '{}': {}", getName(), total);
        return total;
    }
}
