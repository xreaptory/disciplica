package model.domain.model;

import model.domain.contract.Trackable;
import model.domain.exception.HabitNotFoundException;
import model.domain.exception.InvalidHabitException;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class User implements Trackable {
    private static final Logger logger = LoggerFactory.getLogger(User.class);
    private final String username;
    private final IntegerProperty levelProperty;
    private final IntegerProperty experienceProperty;
    private final DoubleProperty completionPercentProperty;
    private String title;
    private final ArrayList<AbstractTask> tasks;

    public User(String username) {
        this.username = username;
        title = "Beginner";
        levelProperty = new SimpleIntegerProperty(this, "level", 1);
        experienceProperty = new SimpleIntegerProperty(this, "experience", 0);
        completionPercentProperty = new SimpleDoubleProperty(this, "completionPercent", 0.0);
        tasks = new ArrayList<>();
    }

    public IntegerProperty levelProperty() {
        return levelProperty;
    }

    public IntegerProperty experienceProperty() {
        return experienceProperty;
    }

    public DoubleProperty completionPercentProperty() {
        return completionPercentProperty;
    }

    public int getLevel() {
        return levelProperty.get();
    }

    public int getExperience() {
        return experienceProperty.get();
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

    public boolean changeTask(AbstractTask oldTask, AbstractTask newTask) throws InvalidHabitException, HabitNotFoundException {
        if (oldTask == null || newTask == null) {
            logger.warn("Attempted to change a task with null values");
            throw new InvalidHabitException("Old and new tasks cannot be null");
        }
        ensureTaskExists(oldTask);
        rejectDuplicateTask(newTask);
        int index = tasks.indexOf(oldTask);
        tasks.set(index, newTask);
        updateCompletionPercent();
        logger.info("Task changed from {} to {}", oldTask.getName(), newTask.getName());
        return true;
    }

    public boolean addTask(AbstractTask task) throws InvalidHabitException {
        rejectNullTask(task);
        rejectDuplicateTask(task);
        tasks.add(task);
        updateCompletionPercent();
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
        updateCompletionPercent();
        logger.info("Task removed: {}", task.getName());
        return task;
    }

    public ArrayList<AbstractTask> getTasks() {
        return tasks;
    }

    public boolean completeTask(AbstractTask task) throws HabitNotFoundException {
        if (task == null) return handleNullCompletion();
        ensureTaskExists(task);
        boolean completedNow = completeAndReward(task);
        updateCompletionPercent();
        return completedNow;
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
        experienceProperty.set(experienceProperty.get() + awardedExperience);
        applyLevelUps();
        updateTitle();
    }

    private void applyLevelUps() {
        while (isReadyForNextLevel()) {
            consumeLevelRequirement();
            levelProperty.set(levelProperty.get() + 1);
        }
    }

    private boolean isReadyForNextLevel() {
        return experienceProperty.get() >= requiredExperienceForCurrentLevel();
    }

    private int requiredExperienceForCurrentLevel() {
        return levelProperty.get() * 50;
    }

    private void consumeLevelRequirement() {
        experienceProperty.set(experienceProperty.get() - requiredExperienceForCurrentLevel());
    }

    private void updateTitle() {
        title = resolveTitle(levelProperty.get());
    }

    private void updateCompletionPercent() {
        completionPercentProperty.set(getProgress() / 100.0);
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
        return "Username: " + username + "; Level: " + levelProperty.get() + "; Exp: "
            + experienceProperty.get()
                + "; Tasks: " + tasks.size() + "; Titel: " + title;
    }

    public void printUser() {
        System.out.println("User: " + username);
        System.out.println("Titel: " + title);
        System.out.println("Level: " + levelProperty.get());
        System.out.println("Exp: " + experienceProperty.get());
    }
}


