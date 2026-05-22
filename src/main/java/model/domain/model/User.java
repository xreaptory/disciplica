package model.domain.model;

import model.domain.contract.Trackable;
import model.domain.exception.HabitNotFoundException;
import model.domain.exception.InvalidHabitException;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class User implements Trackable {
    private static final Logger logger = LoggerFactory.getLogger(User.class);
    private String username;
    private int level;
    private int experience;
    private String title;
    private final ArrayList<AbstractTask> tasks;
    private final Map<String, Integer> completionsByDate;
    private final ArrayList<Integer> xpHistory;

    public User(String username) {
        this.username = username;
        title = "Beginner";
        level = 1;
        experience = 0;
        tasks = new ArrayList<>();
        completionsByDate = new HashMap<>();
        xpHistory = new ArrayList<>();
        xpHistory.add(experience);
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

        for (AbstractTask t : tasks) {
            if (t.getName().equals(newTask.getName()) && t != oldTask) {
                logger.error("Attempted to change task name to an already existing name: {}", newTask.getName());
                throw new InvalidHabitException("Task with the same name already exists: " + newTask.getName());
            }
        }

        int index = tasks.indexOf(oldTask);
        tasks.set(index, newTask);
        logger.info("Task changed from {} to {}", oldTask.getName(), newTask.getName());
        return true;
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

    public AbstractTask getTaskName(String name){
        for(AbstractTask t : tasks){
            if(t.getName().equals(name)){
                return t;
            }
        }
        return null;
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
        for (AbstractTask t : tasks) {
            if (t.getName().equals(task.getName())) {
                logger.error("Attempted to add a duplicate task: {}", task.getName());
                throw new InvalidHabitException("Task with the same name already exists: " + task.getName());
            }
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
        recordCompletionNow();
        grantExperience(task.calculatePoints());
        logger.info("Task completed and XP granted: {}", task.getName());
        return true;
    }

    private void grantExperience(int awardedExperience) {
        experience += awardedExperience;
        applyLevelUps();
        updateTitle();
        appendXpHistory(experience);
    }

    private void applyLevelUps() {
        while (isReadyForNextLevel()) {
            consumeLevelRequirement();
            level++;
        }
    }

    public int getLevel() {
        return level;
    }

    public int getExperience() {
        return experience;
    }

    public String getTitle() {
        return title;
    }

    public User getUser() {
        return this;
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

    private void recordCompletionNow() {
        String today = LocalDate.now().toString();
        completionsByDate.put(today, completionsByDate.getOrDefault(today, 0) + 1);
    }

    private void appendXpHistory(int currentExperience) {
        xpHistory.add(currentExperience);
        int maxHistoryEntries = 200;
        if (xpHistory.size() > maxHistoryEntries) {
            xpHistory.remove(0);
        }
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

    public int getCompletionCountForDate(LocalDate date) {
        return completionsByDate.getOrDefault(date.toString(), 0);
    }

    public List<Integer> getXpHistoryWindow(int size) {
        ArrayList<Integer> result = new ArrayList<>();
        int start = Math.max(0, xpHistory.size() - size);
        for (int i = start; i < xpHistory.size(); i++) {
            result.add(xpHistory.get(i));
        }
        while (result.size() < size) {
            result.add(0, experience);
        }
        return result;
    }

    public void writeTaskTxt() throws IOException {
        String path = System.getProperty("user.dir") + "/data/tasks.txt";
        File file = new File(path);
        file.getParentFile().mkdirs();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (AbstractTask t : tasks) {
                String type = t instanceof DailyHabit ? "D" : (t instanceof WeeklyHabit ? "W" : "O");
                String line = String.format("%s;%s;%s;%d;%b;%d",
                        type,
                        t.getName(),
                        t.getDescription(),
                        t.calculatePoints(),
                        t.isCompleted(),
                        t.getStreak()
                );
                bw.write(line);
                bw.newLine();
            }
            logger.info("Tasks successfully written to {}", path);
        } catch (IOException e) {
            logger.error("Failed to write tasks to file: {}", e.getMessage());
            throw e;
        }
    }

    public void readTaskTxt() throws IOException {
        String path = System.getProperty("user.dir") + "/data/tasks.txt";
        File file = new File(path);
        if (!file.exists()) return; // Nichts zu tun, wenn keine Datei da ist

        tasks.clear();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length < 6) continue;

                AbstractTask task = null;
                String type = parts[0];
                String name = parts[1];
                String desc = parts[2];
                int points = Integer.parseInt(parts[3]);
                boolean isDone = Boolean.parseBoolean(parts[4]);
                int streak = Integer.parseInt(parts[5]);

                if (type.equals("D")) {
                    DailyHabit dh = new DailyHabit(name, desc, points);
                    dh.setStreak(streak);
                    task = dh;
                } else if (type.equals("W")) {
                    WeeklyHabit wh = new WeeklyHabit(name, desc, points);
                    wh.setStreak(streak);
                    task = wh;
                } else if (type.equals("O")) {
                    task = new OneTimeTask(name, desc, points);
                }

                if (task != null) {
                    task.setCompleted(isDone);
                    tasks.add(task);
                }
            }
        } catch (InvalidHabitException e) {
            logger.error("Invalid habit data in file: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void writeUserTxt() throws IOException {
        String path = System.getProperty("user.dir") + "/data/user.txt";
        File file = new File(path);
        file.getParentFile().mkdirs();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            String line = String.format("%s;%d;%d;%s",
                    username, level, experience, title);
            bw.write(line);
            bw.newLine();
            bw.write("completions=" + serializeCompletions());
            bw.newLine();
            bw.write("xpHistory=" + serializeXpHistory());
            logger.info("User data successfully written to {}", path);
        } catch (IOException e) {
            logger.error("Failed to write user data to file: {}", e.getMessage());
            throw e;
        }
    }

    public void readUserTxt() throws IOException {
        String path = System.getProperty("user.dir") + "/data/user.txt";
        File file = new File(path);
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            if (line == null) return;

            String[] parts = line.split(";");
            if (parts.length < 4) return;

            username = parts[0];
            level = Integer.parseInt(parts[1]);
            experience = Integer.parseInt(parts[2]);
            title = parts[3];

            completionsByDate.clear();
            xpHistory.clear();
            xpHistory.add(experience);

            String extraLine;
            while ((extraLine = br.readLine()) != null) {
                if (extraLine.startsWith("completions=")) {
                    parseCompletions(extraLine.substring("completions=".length()));
                } else if (extraLine.startsWith("xpHistory=")) {
                    parseXpHistory(extraLine.substring("xpHistory=".length()));
                }
            }
        } catch (IOException e) {
            logger.error("Failed to read user data from file: {}", e.getMessage());
            throw e;
        }
    }

    private String serializeCompletions() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Integer> entry : completionsByDate.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            sb.append(entry.getKey()).append(":").append(entry.getValue());
            first = false;
        }
        return sb.toString();
    }

    private String serializeXpHistory() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < xpHistory.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(xpHistory.get(i));
        }
        return sb.toString();
    }

    private void parseCompletions(String payload) {
        if (payload == null || payload.isBlank()) {
            return;
        }
        String[] entries = payload.split(",");
        for (String entry : entries) {
            String[] keyValue = entry.split(":");
            if (keyValue.length != 2) {
                continue;
            }
            completionsByDate.put(keyValue[0], Integer.parseInt(keyValue[1]));
        }
    }

    private void parseXpHistory(String payload) {
        if (payload == null || payload.isBlank()) {
            return;
        }
        xpHistory.clear();
        String[] values = payload.split(",");
        for (String value : values) {
            xpHistory.add(Integer.parseInt(value));
        }
        if (xpHistory.isEmpty()) {
            xpHistory.add(experience);
        }
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


