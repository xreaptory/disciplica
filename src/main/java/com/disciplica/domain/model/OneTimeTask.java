package com.disciplica.domain.model;

import com.disciplica.domain.exception.InvalidHabitException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OneTimeTask extends AbstractTask {
    private static final Logger logger = LoggerFactory.getLogger(OneTimeTask.class);

    @JsonCreator
    public OneTimeTask(OneTimeTaskState state) throws InvalidHabitException {
        super(state.name(), state.description(), state.points());
        restoreCompletedState(state.completed());
        logger.debug("OneTimeTask created: name='{}', points={}, completed={}", state.name(),
                state.points(), state.completed());
    }

    public OneTimeTask(String name, String description, int points) throws InvalidHabitException {
        this(new OneTimeTaskState(name, description, points, false));
    }

    private void restoreCompletedState(boolean completed) {
        if (completed) {
            super.complete();
        }
    }

    @Override
    public int calculatePoints() {
        int awardedPoints = super.getPoints();
        logger.debug("Calculated points for OneTimeTask '{}': {}", getName(), awardedPoints);
        return awardedPoints;
    }

    @Override
    @JsonIgnore
    public int getStreak() {
        return 0;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OneTimeTaskState(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("points") int points,
            @JsonProperty("completed") boolean completed) {
    }
}


