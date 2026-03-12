package com.disciplica;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DailyHabit extends AbstractTask {
    private static final Logger logger = LoggerFactory.getLogger(DailyHabit.class);
    private int streak;

    public DailyHabit(String name, String description, int points) throws InvalidHabitException {
        super(name, description, points);
        this.streak = 0;
        logger.debug("DailyHabit created: name='{}', points={}", name, points);
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
