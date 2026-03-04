package org.example.disciplica;


import java.util.ArrayList;
import java.util.Random;

public class User {
    private String username;
    private int level;
    private int Exp;
    private ArrayList<Habit> habits;

    public int getExp() {
        return Exp;
    }

    public void setExp(int exp) {
        Exp = exp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean addHabit(Habit habit){
        if(habit!=null && !habits.contains(habit)){
            habits.add(habit);
            return true;
        }
        return false;
    }

    public Habit removeHabit(Habit habit){
        if(habit!=null && habits.contains(habit)){
            Habit temp = habit;
            habits.remove(habit);
            return temp;
        }
        return null;
    }

    public boolean completeHabit(Habit habit){
        if(habit!=null && habits.contains(habit)){
            habit.complete();
            Random random = new Random();
            Exp += random.nextInt(5,11);
            checkLevelUp();
            return true;
        }
        return false;
    }

    public void checkLevelUp(){
        if(Exp >= level*50){
            level++;
            Exp -= level*50;
        }
    }
}
