package org.example.disciplica;

public class WeeklyHabit extends AbstractTask {
    private int streak;

    public WeeklyHabit(String name, String description, int points) {
        super(name, description, points);
        this.streak = 0;
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
        // Weekly habits get a bigger bonus for streak
        return super.getPoints() + (streak * 15);
    }

    @Override
    public String toString() {
        return super.toString() + " [Streak: " + streak + "]";
    }
}
