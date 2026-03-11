package org.example.disciplica;


import java.util.ArrayList;
import java.util.Random;

public class User {
    private String username;
    private int level;
    private int Exp;
    private String titel;
    private ArrayList<Habit> habits;

    public User(String username) {
        this.username = username;
        titel = "Beginner";
        level = 1;
        Exp = 0;
        habits = new ArrayList<>();
    }

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

    public ArrayList<Habit> getHabits() {
        return habits;
    }

    public boolean completeHabit(Habit habit){
        if(habit!=null && habits.contains(habit)){
            if(habit.complete()){
                Random random = new Random();
                Exp += random.nextInt(20,26);
                checkLevelUp();
                checkTitel();
                return true;
            }
        }
        return false;
    }

    public void checkLevelUp(){
        while (true){
            if(Exp >= level*50){
                Exp -= level*50;
                level++;
            }
            else {
                break;
            }
        }
    }

    public void checkTitel(){
        if(level>=5&&level<10){
            titel = "Novice";
        }
        else if(level>=10&&level<15){
            titel = "Apprentice";
        }
        else if(level>=15&&level<20){
            titel = "Expert";
        }
        else if(level>=20&&level<25){
            titel = "Legend";
        }
        else if(level>=25){
            titel = "Master";
        }
    }

    public void printHabits(){
        for(Habit h : habits){
            System.out.println(h.toString());
        }
    }

    @Override
    public String toString() {
        return "Username: " + username + "; Level: " + level + "; Exp: " + Exp + "; Habits: " + habits.size()+"; Titel: "+titel;
    }

    public void printUser(){
        System.out.println(toString());
    }
}
