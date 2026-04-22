package com.disciplica.domain.model;

import com.disciplica.domain.contract.Trackable;
import com.disciplica.domain.exception.HabitNotFoundException;
import com.disciplica.domain.exception.InvalidHabitException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class User implements Trackable {
    private static final Logger logger = LoggerFactory.getLogger(User.class);
    private final String username;
    private int level;
    private int experience;
    private String title;
    private final ArrayList<AbstractTask> tasks;

    public User(String username) {
        this.username = username;
        title = "Beginner";
        level = 1;
        experience = 0;
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
                .mapToInt(task -> {
                    if (task instanceof DailyHabit) return ((DailyHabit) task).getStreak();
                    if (task instanceof WeeklyHabit) return ((WeeklyHabit) task).getStreak();
                    return 0;
                })
                .max()
                .orElse(0);
    }

    public boolean addTask(AbstractTask task) throws InvalidHabitException {
        rejectNullTask(task);
        rejectDuplicateTask(task);
        tasks.add(task);
        logger.info("Task added: {}", task.getName());
        return true;
    }

    public String[] getAllHabits() {
        String[] habits = new String[tasks.size()];
        int i = 0;
        for (AbstractTask task : tasks) {
            habits[i++] = task.toString();
        }
        return habits;
    }

    public AbstractTask removeTask(AbstractTask task) throws HabitNotFoundException {
        if (task == null) return handleNullRemoval();
        ensureTaskExists(task);
        tasks.remove(task);
        logger.info("Task removed: {}", task.getName());
        return task;
    }

    public ArrayList<AbstractTask> getTasks() {
        return tasks;
    }

    public boolean completeTask(AbstractTask task) throws HabitNotFoundException {
        if (task == null) return handleNullCompletion();
        ensureTaskExists(task);
        return completeAndReward(task);
    }

    private void rejectNullTask(AbstractTask task) throws InvalidHabitException {
        if (task == null) {
            logger.error("Attempted to add a null task");
            throw new InvalidHabitException("Cannot add a null task");
        }
    }

    private void rejectDuplicateTask(AbstractTask task) throws InvalidHabitException {
        if (tasks.contains(task)) {
            logger.warn("Attempted to add duplicate task: {}", task.getName());
            throw new InvalidHabitException("Task already exists: " + task.getName());
        }
    }

    private AbstractTask handleNullRemoval() {
        logger.warn("Attempted to remove a null task");
        return null;
    }

    private void ensureTaskExists(AbstractTask task) throws HabitNotFoundException {
        if (!tasks.contains(task)) {
            logger.error("Attempted to operate on non-existent task: {}", task.getName());
            throw new HabitNotFoundException("Task not found: " + task.getName());
        }
    }

    private boolean handleNullCompletion() {
        logger.warn("Attempted to complete a null task");
        return false;
    }

    private boolean completeAndReward(AbstractTask task) {
        if (!task.complete()) {
            return false;
        }
        grantExperience(task.calculatePoints());
        logger.info("Task completed and XP granted: {}", task.getName());
        return true;
    }

    private void grantExperience(int awardedExperience) {
        experience += awardedExperience;
        applyLevelUps();
        updateTitle();
    }

    private void applyLevelUps() {
        while (isReadyForNextLevel()) {
            consumeLevelRequirement();
            level++;
        }
    }

    private boolean isReadyForNextLevel() {
        return experience >= requiredExperienceForCurrentLevel();
    }

    private int requiredExperienceForCurrentLevel() {
        return level * 50;
    }

    private void consumeLevelRequirement() {
        experience -= requiredExperienceForCurrentLevel();
    }

    private void updateTitle() {
        title = resolveTitle(level);
    }

    private String resolveTitle(int currentLevel) {
        if (currentLevel >= 25) return "Master";
        if (currentLevel >= 20) return "Legend";
        if (currentLevel >= 15) return "Expert";
        if (currentLevel >= 10) return "Apprentice";
        if (currentLevel >= 5) return "Novice";
        return "Beginner";
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
        return "Username: " + username + "; Level: " + level + "; Exp: " + experience
                + "; Tasks: " + tasks.size() + "; Titel: " + title;
    }

    public void printUser() {
        System.out.println("User: " + username);
        System.out.println("Titel: " + title);
        System.out.println("Level: " + level);
        System.out.println("Exp: " + experience);
    }
}


