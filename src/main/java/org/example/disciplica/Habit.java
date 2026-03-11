package org.example.disciplica;

public class Habit implements Completable, Trackable {
    private String name;
    private String description;
    private boolean isCompleted;
    private int streak;

    public Habit(String name, String description) {
        setName(name);
        setDescription(description);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void getProgress() {

    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public boolean complete() {
        if (isCompleted) {
            return false;
        }
        isCompleted = true;
        streak++;
        return true;
    }

    @Override
    public void getReward() {

    }

    public void resetStreak(){
        streak = 0;
        System.out.println("streak reset");
    }

    public void print(){
        System.out.println("Habit: " + name);
    }

    @Override
    public String toString() {
        return "Name: " + name + "; Description: " + description + "; isCompleted: " + isCompleted + "; Streak: " + streak;
    }
}
