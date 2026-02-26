package org.example.disciplica;

public class Habit {
    private String name;
    private String description;
    private boolean isCompleted;

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

    public boolean isCompleted() {
        return isCompleted;
    }

    public void complete() {
        isCompleted = true;
    }

    public void print(){
        System.out.println("Habit: " + name);
    }
}
