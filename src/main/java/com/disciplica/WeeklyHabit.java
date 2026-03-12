package com.disciplica;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeeklyHabit extends AbstractTask {
    private static final Logger logger = LoggerFactory.getLogger(WeeklyHabit.class);
    private int streak;

    public WeeklyHabit(String name, String description, int points) throws InvalidHabitException {
        super(name, description, points);
        this.streak = 0;
        logger.debug("WeeklyHabit created: name='{}', points={}", name, points);
    }

    @Override
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
        if (completedNow) {
            streak++;
            logger.info("WeeklyHabit '{}' completed. New streak: {}", getName(), streak);
        } else {
            logger.warn("WeeklyHabit '{}' was already completed, streak unchanged", getName());
        }
        return completedNow;
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
        return super.toString() + " [Streak: " + streak + "]";
    }
}
