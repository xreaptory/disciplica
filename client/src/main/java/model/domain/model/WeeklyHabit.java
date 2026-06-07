package model.domain.model;

import model.domain.exception.InvalidHabitException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "weekly_habits")
@DiscriminatorValue("WEEKLY")
public class WeeklyHabit extends Task {
    private static final Logger logger = LoggerFactory.getLogger(WeeklyHabit.class);
    @JsonProperty("streak")
    private int streak;

    @JsonCreator
    public WeeklyHabit(WeeklyHabitState state) throws InvalidHabitException {
        super(state.name(), state.description(), state.points());
        streak = state.streak();
        restoreCompletedState(state.completed());
        logger.debug("WeeklyHabit created: name='{}', points={}, streak={}, completed={}", state.name(),
                state.points(), streak, state.completed());
    }

    public WeeklyHabit(String name, String description, int points) throws InvalidHabitException {
        super(name,description,points);
        streak = 0;
    }

    protected WeeklyHabit() {
        super();
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
        logger.info("Resetting streak for WeeklyHabit '{}', was {}", getName(), streak);
        streak = 0;
        logger.debug("Streak reset complete for '{}'", getName());
    }

    @Override
    public boolean complete() {
        logger.debug("Attempting to complete WeeklyHabit '{}'", getName());
        boolean completedNow = super.complete();
        updateStreakAfterCompletion(completedNow);
        return completedNow;
    }

    private void updateStreakAfterCompletion(boolean completedNow) {
        if (completedNow) {
            streak++;
            logger.info("WeeklyHabit '{}' completed. New streak: {}", getName(), streak);
            return;
        }
        logger.warn("WeeklyHabit '{}' was already completed, streak unchanged", getName());
    }

    public void setStreak(int streak) {
        if(streak < 0) {
            logger.warn("Attempted to set a negative streak value for DailyHabit '{}'", getName());
            throw new IllegalArgumentException("Streak cannot be negative");
        }
        logger.info("Setting streak for DailyHabit '{}', was {}, now {}", getName(), this.streak, streak);
        this.streak = streak;
    }

    @Override
    public int calculatePoints() {
        int total = super.getPoints() + (streak * 15);
        logger.debug("Calculated points for WeeklyHabit '{}': base={} + streakBonus={} = {}",
                getName(), super.getPoints(), streak * 15, total);
        return total;
    }

    @Override
    public String toString() {
        return "W;"+super.toString() +";"+streak;
    }

    public record WeeklyHabitState(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("points") int points,
            @JsonProperty("streak") int streak,
            @JsonProperty("completed") boolean completed) {
    }
}


