package com.disciplica;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class User implements Trackable {
    private static final Logger logger = LoggerFactory.getLogger(User.class);
    private final String username;
    private int level;
    private int Exp;
    private String titel;
    private final ArrayList<AbstractTask> tasks;

    public User(String username) {
        this.username = username;
        titel = "Beginner";
        level = 1;
        Exp = 0;
        tasks = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public int getProgress() {
        if (tasks.isEmpty()) return 0;
        long completed = tasks.stream().filter(AbstractTask::isCompleted).count();
        return (int) ((completed * 100) / tasks.size());
    }

    @Override
    public int getStreak() {
        return tasks.stream()
                .mapToInt(t -> {
                    if (t instanceof DailyHabit) return ((DailyHabit) t).getStreak();
                    if (t instanceof WeeklyHabit) return ((WeeklyHabit) t).getStreak();
                    return 0;
                })
                .max()
                .orElse(0);
    }

    public boolean addTask(AbstractTask task) throws InvalidHabitException {
        if (task == null) {
            logger.error("Attempted to add a null task");
            throw new InvalidHabitException("Cannot add a null task");
        }
        if (tasks.contains(task)) {
            logger.warn("Attempted to add duplicate task: {}", task.getName());
            throw new InvalidHabitException("Task already exists: " + task.getName());
        }
        tasks.add(task);
        logger.info("Task added: {}", task.getName());
        return true;
    }

    public AbstractTask removeTask(AbstractTask task) throws HabitNotFoundException {
        if (task == null) {
            logger.warn("Attempted to remove a null task");
            return null;
        }
        if (!tasks.contains(task)) {
            logger.error("Attempted to remove non-existent task: {}", task.getName());
            throw new HabitNotFoundException("Task not found: " + task.getName());
        }
        tasks.remove(task);
        logger.info("Task removed: {}", task.getName());
        return task;
    }

    public ArrayList<AbstractTask> getTasks() {
        return tasks;
    }

    public boolean completeTask(AbstractTask task) throws HabitNotFoundException {
        if (task == null) {
             logger.warn("Attempted to complete a null task");
             return false;
        }
        if (!tasks.contains(task)) {
            logger.error("Attempted to complete non-existent task: {}", task.getName());
            throw new HabitNotFoundException("Task not found for completion: " + task.getName());
        }
        if (task.complete()) {
            Exp += task.calculatePoints();
            checkLevelUp();
            checkTitel();
            logger.info("Task completed and XP granted: {}", task.getName());
            return true;
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
        tasks.forEach(System.out::println);
    }

    public List<AbstractTask> getCompletedTasks() {
        return tasks.stream()
                .filter(AbstractTask::isCompleted)
                .toList();
    }

    public List<String> getTaskNames() {
        return tasks.stream()
                .map(AbstractTask::getName)
                .toList();
    }

    public int getTotalExperienceFromTasks() {
        return tasks.stream()
                .map(AbstractTask::calculatePoints)
                .reduce(0, Integer::sum);
    }

    public List<AbstractTask> getTasksSortedByStreak() {
        return tasks.stream()
                .sorted(Comparator.comparingInt(AbstractTask::getStreak).reversed())
                .toList();
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
