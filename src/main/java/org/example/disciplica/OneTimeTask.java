package org.example.disciplica;

public class OneTimeTask extends AbstractTask {

    public OneTimeTask(String name, String description, int points) {
        super(name, description, points);
    }

    @Override
    public int calculatePoints() {
        // One time tasks just give the base points
        return super.getPoints();
    }
}
