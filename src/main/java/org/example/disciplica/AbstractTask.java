package org.example.disciplica;

public abstract class AbstractTask {
    private String name;
    private String description;
    private boolean isCompleted;
    private int points; // Base points

    public AbstractTask(String name, String description, int points) {
        this.name = name;
        this.description = description;
        this.points = points;
        this.isCompleted = false;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public int getPoints() {
        return points;
    }

    public boolean complete() {
        if (!isCompleted) {
            isCompleted = true;
            return true;
        }
        return false;
    }

    public abstract int calculatePoints();

    @Override
    public String toString() {
        return "Name: " + name + " (" + this.getClass().getSimpleName() + ") - " + (isCompleted ? "[DONE]" : "[ ]");
    }
}


