package org.example.disciplica;

public class Habit {
    private String name;
    private String description;
    private boolean isCompleted;

    public Habit(String name, String description, boolean isCompleted) {
        this.name = name;
        this.description = description;
        this.isCompleted = false;
    }

    public void print(){
        System.out.println("Habit: " + name);
    }
}
