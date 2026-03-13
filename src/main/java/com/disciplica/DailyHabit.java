package com.disciplica;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DailyHabit extends AbstractTask {
    private static final Logger logger = LoggerFactory.getLogger(DailyHabit.class);
    private int streak;

    @JsonCreator
    public DailyHabit(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("points") int points,
            @JsonProperty("streak") int streak,
            @JsonProperty("completed") boolean completed) throws InvalidHabitException {
        super(name, description, points);
        this.streak = streak;
        if (completed) {
            super.complete(); // Restore completed state
        }
        logger.debug("DailyHabit created: name='{}', points={}, streak={}, completed={}", name, points, streak, completed);
    }

    // Convenience constructor for creating new habits
    public DailyHabit(String name, String description, int points) throws InvalidHabitException {
        this(name, description, points, 0, false);
    }

    @Override
    public int getStreak() {
        return streak;
    }

    public void resetStreak() {
        logger.info("Resetting streak for DailyHabit '{}', was {}", getName(), streak);
        streak = 0;
        logger.debug("Streak reset complete for '{}'", getName());
    }

    @Override
    public boolean complete() {
        logger.debug("Attempting to complete DailyHabit '{}'", getName());
        boolean completedNow = super.complete();
        if (completedNow) {
            streak++;
            logger.info("DailyHabit '{}' completed. New streak: {}", getName(), streak);
        } else {
            logger.warn("DailyHabit '{}' was already completed, streak unchanged", getName());
        }
        return completedNow;
    }

    @Override
    public int calculatePoints() {
        int total = super.getPoints() + (streak * 5);
        logger.debug("Calculated points for DailyHabit '{}': base={} + streakBonus={} = {}",
                getName(), super.getPoints(), streak * 5, total);
        return total;
    }

    @Override
    public String toString() {
        return super.toString() + " [Streak: " + streak + "]";
    }
}
