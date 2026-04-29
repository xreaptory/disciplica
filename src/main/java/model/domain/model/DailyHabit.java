package model.domain.model;

import model.domain.exception.InvalidHabitException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DailyHabit extends AbstractTask {
    private static final Logger logger = LoggerFactory.getLogger(DailyHabit.class);
    @JsonProperty("streak")
    private int streak;

    @JsonCreator
    public DailyHabit(DailyHabitState state) throws InvalidHabitException {
        super(state.name(), state.description(), state.points());
        streak = state.streak();
        restoreCompletedState(state.completed());
        logger.debug("DailyHabit created: name='{}', points={}, streak={}, completed={}", state.name(),
                state.points(), streak, state.completed());
    }

    public DailyHabit(String name, String description, int points) throws InvalidHabitException {
        super(name,description,points);
        streak = 0;

    }

    private void restoreCompletedState(boolean completed) {
        if (completed) {
            super.complete();
        }
    }

    @Override
    @JsonProperty("streak")
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
        updateStreakAfterCompletion(completedNow);
        return completedNow;
    }

    private void updateStreakAfterCompletion(boolean completedNow) {
        if (completedNow) {
            streak++;
            logger.info("DailyHabit '{}' completed. New streak: {}", getName(), streak);
            return;
        }
        logger.warn("DailyHabit '{}' was already completed, streak unchanged", getName());
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
        return super.toString() +";"+streak;
    }

    public record DailyHabitState(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("points") int points,
            @JsonProperty("streak") int streak,
            @JsonProperty("completed") boolean completed) {
    }
}


