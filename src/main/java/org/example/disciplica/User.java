package org.example.disciplica;


import java.util.ArrayList;

public class User {
    private String username;
    private int level;
    private int Exp;
    private String titel;
    private ArrayList<Task> tasks;

    public User(String username) {
        this.username = username;
        titel = "Beginner";
        level = 1;
        Exp = 0;
        tasks = new ArrayList<>();
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

    public boolean addTask(org.example.disciplica.DailyHabit task){
        if(task!=null && !tasks.contains(task)){
            tasks.add(task);
            return true;
        }
        return false;
    }

    public Task removeTask(Task task){
        if(task!=null && tasks.contains(task)){
            Task temp = task;
            tasks.remove(task);
            return temp;
        }
        return null;
    }

    public ArrayList<Task> getTasks() {
        return tasks;
    }

    public boolean completeTask(Task task){
        if(task!=null && tasks.contains(task)){
            if(task.complete()){
                Exp += task.calculatePoints();
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

    public void printTasks() {
        for (AbstractTask task : tasks) {
            System.out.println(task);
        }
    }

    @Override
    public String toString() {
        return "Username: " + username + "; Level: " + level + "; Exp: " + Exp + "; Tasks: " + tasks.size()+"; Titel: "+titel;
    }

    public void printUser(){
        System.out.println("User: "+username);
        System.out.println("Titel: "+titel);
        System.out.println("Level: "+level);
        System.out.println("Exp: "+Exp);
    }
}
