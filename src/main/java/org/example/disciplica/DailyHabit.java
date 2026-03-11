package org.example.disciplica;

public class DailyHabit extends AbstractTask {
    private int streak;

    public DailyHabit(String name, String description, int points) {
        super(name, description, points);
        this.streak = 0;
    }

    public int getStreak() {
        return streak;
    }

    public void resetStreak() {
        streak = 0;
    }

    @Override
    public boolean complete() {
        boolean completedNow = super.complete();
        if (completedNow) {
            streak++;
        }
        return completedNow;
    }

    @Override
    public int calculatePoints() {
        // Daily habits get a bonus for streak
        return super.getPoints() + (streak * 5);
    }

    @Override
    public String toString() {
        return super.toString() + " [Streak: " + streak + "]";
    }
}
