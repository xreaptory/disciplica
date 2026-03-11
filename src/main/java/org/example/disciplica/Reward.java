package org.example.disciplica;

public class Reward {
    private String name;
    private String description;
    private int pointsRequired;

    public Reward(String name, String description, int pointsRequired) {
        this.name = name;
        this.description = description;
        this.pointsRequired = pointsRequired;
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

    public int getPointsRequired() {
        return pointsRequired;
    }

    public void setPointsRequired(int pointsRequired) {
        this.pointsRequired = pointsRequired;
    }

}
