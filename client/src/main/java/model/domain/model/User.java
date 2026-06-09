package model.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import model.domain.contract.Trackable;
import model.domain.exception.HabitNotFoundException;
import model.domain.exception.InvalidHabitException;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Der Spieler bzw. Benutzer der Anwendung.
 * <p>
 * Bündelt den gesamten Spielfortschritt: Aufgabenliste, Level,
 * Erfahrungspunkte, Gold, Lebenspunkte und Titel. Stellt die Spiellogik zum
 * Erledigen von Aufgaben (mit Belohnungen und Stufenaufstiegen) bereit und
 * kann den eigenen Zustand in einfache Textdateien sichern bzw. daraus laden.
 * Bildet zugleich als JPA-Entität die Tabelle {@code users} ab.
 */
@Entity
@Table(name = "users")
public class User implements Trackable {
    private static final Logger logger = LoggerFactory.getLogger(User.class);
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "username", nullable = false, unique = true)
    private String username;
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Column(name = "level", nullable = false)
    private int level;
    @Column(name = "xp", nullable = false)
    private int experience;
    @Column(name = "gold", nullable = false)
    private int gold;
    @Column(name = "health", nullable = false)
    private int health;
    @Transient
    private String title;
    @Transient
    private final ArrayList<AbstractTask> tasks;
    @Transient
    private final Map<String, Integer> completionsByDate;
    @Transient
    private final ArrayList<Integer> xpHistory;
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = jakarta.persistence.CascadeType.ALL, orphanRemoval = true)
    private List<Habit> habits = new ArrayList<>();

    /**
     * Erzeugt einen leeren Benutzer mit Standardwerten (wird von der
     * Persistenzschicht benötigt).
     */
    protected User() {
        this.username = "";
        this.email = "unknown@local";
        this.title = "Beginner";
        this.level = 1;
        this.experience = 0;
        this.gold = 0;
        this.health = 50;
        this.tasks = new ArrayList<>();
        this.completionsByDate = new HashMap<>();
        this.xpHistory = new ArrayList<>();
        this.xpHistory.add(this.experience);
    }

    /**
     * Erzeugt einen neuen Benutzer mit Standardwerten für den Spielstart.
     *
     * @param username der Benutzername
     */
    public User(String username) {
        this.username = username;
        this.email = username.toLowerCase() + "@local";
        title = "Beginner";
        level = 1;
        experience = 0;
        gold = 0;
        health = 50;
        tasks = new ArrayList<>();
        completionsByDate = new HashMap<>();
        xpHistory = new ArrayList<>();
        xpHistory.add(experience);
    }

    /**
     * {@return der Benutzername}
     */
    public String getUsername() {
        return username;
    }

    /**
     * {@return die Datenbank-Kennung oder {@code null}, wenn noch nicht
     * gespeichert}
     */
    public Long getId() {
        return id;
    }

    /**
     * {@return die E-Mail-Adresse des Benutzers}
     */
    public String getEmail() {
        return email;
    }

    /**
     * {@return die Liste der Gewohnheiten dieses Benutzers}
     */
    public List<Habit> getHabits() {
        return habits;
    }

    /**
     * Fügt dem Benutzer eine Gewohnheit hinzu und verknüpft sie mit ihm.
     *
     * @param habit die hinzuzufügende Gewohnheit
     */
    public void addHabit(Habit habit) {
        habits.add(habit);
        habit.setUser(this);
    }

    /**
     * {@return der Name des Benutzers (entspricht dem Benutzernamen)}
     */
    @Override
    public String getName() {
        return username;
    }

    /**
     * Berechnet den Gesamtfortschritt als Anteil erledigter Aufgaben in
     * Prozent.
     *
     * @return der Fortschritt in Prozent (0, wenn keine Aufgaben vorhanden)
     */
    @Override
    public int getProgress() {
        if (tasks.isEmpty()) return 0;
        long completed = tasks.stream().filter(AbstractTask::isCompleted).count();
        return (int) ((completed * 100) / tasks.size());
    }

    /**
     * Ermittelt die längste Serie über alle Gewohnheiten des Benutzers.
     *
     * @return die höchste Serie oder 0, wenn keine vorhanden ist
     */
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

    /**
     * Ersetzt eine vorhandene Aufgabe durch eine neue.
     *
     * @param oldTask die zu ersetzende Aufgabe
     * @param newTask die neue Aufgabe
     * @return {@code true}, wenn die Aufgabe ersetzt wurde
     * @throws InvalidHabitException   wenn ein Wert {@code null} ist oder der
     *                                 neue Name bereits vergeben ist
     * @throws HabitNotFoundException  wenn die alte Aufgabe nicht existiert
     */
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

    /**
     * Fügt der Aufgabenliste eine neue Aufgabe hinzu.
     *
     * @param task die hinzuzufügende Aufgabe
     * @return {@code true}, wenn die Aufgabe hinzugefügt wurde
     * @throws InvalidHabitException wenn die Aufgabe {@code null} ist oder der
     *                               Name bereits vergeben ist
     */
    public boolean addTask(AbstractTask task) throws InvalidHabitException {
        rejectNullTask(task);
        rejectDuplicateTask(task);
        tasks.add(task);
        logger.info("Task added: {}", task.getName());
        return true;
    }

    /**
     * Gibt alle Aufgaben in ihrer Textdarstellung zurück.
     *
     * @return ein Feld mit den Textdarstellungen aller Aufgaben
     */
    public String[] getAllHabits() {
        String[] habits = new String[tasks.size()];
        int i = 0;
        for (AbstractTask task : tasks) {
            habits[i++] = task.toString();
        }
        return habits;
    }

    /**
     * Sucht eine Aufgabe anhand ihres Namens.
     *
     * @param name der gesuchte Aufgabenname
     * @return die gefundene Aufgabe oder {@code null}
     */
    public AbstractTask getTaskName(String name){
        for(AbstractTask t : tasks){
            if(t.getName().equals(name)){
                return t;
            }
        }
        return null;
    }

    /**
     * Entfernt eine Aufgabe aus der Aufgabenliste.
     *
     * @param task die zu entfernende Aufgabe
     * @return die entfernte Aufgabe oder {@code null}, wenn {@code null}
     *         übergeben wurde
     * @throws HabitNotFoundException wenn die Aufgabe nicht existiert
     */
    public AbstractTask removeTask(AbstractTask task) throws HabitNotFoundException {
        if (task == null) return handleNullRemoval();
        ensureTaskExists(task);
        tasks.remove(task);
        logger.info("Task removed: {}", task.getName());
        return task;
    }

    /**
     * {@return die veränderbare Liste aller Aufgaben des Benutzers}
     */
    public ArrayList<AbstractTask> getTasks() {
        return tasks;
    }

    /**
     * Schließt eine Aufgabe ab und vergibt – falls erfolgreich – die
     * Belohnung.
     *
     * @param task die abzuschließende Aufgabe
     * @return {@code true}, wenn die Aufgabe erfolgreich abgeschlossen wurde
     * @throws HabitNotFoundException wenn die Aufgabe nicht existiert
     */
    public boolean completeTask(AbstractTask task) throws HabitNotFoundException {
        if (task == null) return handleNullCompletion();
        ensureTaskExists(task);
        return completeAndReward(task);
    }

    /**
     * Weist eine {@code null}-Aufgabe ab.
     *
     * @param task die zu prüfende Aufgabe
     * @throws InvalidHabitException wenn die Aufgabe {@code null} ist
     */
    private void rejectNullTask(AbstractTask task) throws InvalidHabitException {
        if (task == null) {
            logger.error("Attempted to add a null task");
            throw new InvalidHabitException("Cannot add a null task");
        }
    }

    /**
     * Weist eine Aufgabe mit bereits vergebenem Namen ab.
     *
     * @param task die zu prüfende Aufgabe
     * @throws InvalidHabitException wenn der Name bereits vergeben ist
     */
    private void rejectDuplicateTask(AbstractTask task) throws InvalidHabitException {
        for (AbstractTask t : tasks) {
            if (t.getName().equals(task.getName())) {
                logger.error("Attempted to add a duplicate task: {}", task.getName());
                throw new InvalidHabitException("Task with the same name already exists: " + task.getName());
            }
        }
    }

    /**
     * Behandelt den Versuch, eine {@code null}-Aufgabe zu entfernen.
     *
     * @return immer {@code null}
     */
    private AbstractTask handleNullRemoval() {
        logger.warn("Attempted to remove a null task");
        return null;
    }

    /**
     * Stellt sicher, dass eine Aufgabe in der Liste vorhanden ist.
     *
     * @param task die zu prüfende Aufgabe
     * @throws HabitNotFoundException wenn die Aufgabe nicht vorhanden ist
     */
    private void ensureTaskExists(AbstractTask task) throws HabitNotFoundException {
        if (!tasks.contains(task)) {
            logger.error("Attempted to operate on non-existent task: {}", task.getName());
            throw new HabitNotFoundException("Task not found: " + task.getName());
        }
    }

    /**
     * Behandelt den Versuch, eine {@code null}-Aufgabe abzuschließen.
     *
     * @return immer {@code false}
     */
    private boolean handleNullCompletion() {
        logger.warn("Attempted to complete a null task");
        return false;
    }

    /**
     * Schließt eine Aufgabe ab und schreibt Erfahrungspunkte und Gold gut.
     *
     * @param task die abzuschließende Aufgabe
     * @return {@code true}, wenn die Aufgabe abgeschlossen wurde
     */
    private boolean completeAndReward(AbstractTask task) {
        if (!task.complete()) {
            return false;
        }
        recordCompletionNow();
        int xpAward = task.calculatePoints();
        int goldAward = Math.max(1, xpAward / 5);
        grantExperience(xpAward);
        gold += goldAward;
        logger.info("Task completed and rewards granted: {} (xp={}, gold={})", task.getName(), xpAward, goldAward);
        return true;
    }

    /**
     * Schreibt Erfahrungspunkte gut, wendet etwaige Stufenaufstiege an und
     * aktualisiert Titel sowie XP-Verlauf.
     *
     * @param awardedExperience die gutzuschreibenden Erfahrungspunkte
     */
    private void grantExperience(int awardedExperience) {
        experience += awardedExperience;
        applyLevelUps();
        updateTitle();
        appendXpHistory(experience);
    }

    /**
     * Schreibt Erfahrungspunkte und Gold gut (z.&nbsp;B. aus externen
     * Belohnungen) und aktualisiert Level und Titel.
     *
     * @param xpAward   die gutzuschreibenden Erfahrungspunkte
     * @param goldAward das gutzuschreibende Gold
     * @throws IllegalArgumentException wenn ein Wert negativ ist
     */
    public void awardXpAndGold(int xpAward, int goldAward) {
        if (xpAward < 0 || goldAward < 0) {
            throw new IllegalArgumentException("XP and gold awards must be non-negative");
        }
        experience += xpAward;
        gold += goldAward;
        applyLevelUps();
        updateTitle();
        appendXpHistory(experience);
    }

    /**
     * Zieht Lebenspunkte ab (nicht unter 0).
     *
     * @param hpLoss die abzuziehenden Lebenspunkte
     * @throws IllegalArgumentException wenn der Wert negativ ist
     */
    public void applyHealthPenalty(int hpLoss) {
        if (hpLoss < 0) {
            throw new IllegalArgumentException("Health penalty must be non-negative");
        }
        health = Math.max(0, health - hpLoss);
    }

    /**
     * Heilt den Benutzer (höchstens bis zum Maximum von 50).
     *
     * @param hpGain die gutzuschreibenden Lebenspunkte
     * @throws IllegalArgumentException wenn der Wert negativ ist
     */
    public void heal(int hpGain) {
        if (hpGain < 0) {
            throw new IllegalArgumentException("Healing must be non-negative");
        }
        health = Math.min(50, health + hpGain);
    }

    /**
     * Gibt Gold aus.
     *
     * @param amount der auszugebende Betrag
     * @throws IllegalArgumentException wenn der Betrag negativ ist oder das
     *                                  Gold nicht ausreicht
     */
    public void spendGold(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Gold spend amount must be non-negative");
        }
        if (amount > gold) {
            throw new IllegalArgumentException("Not enough gold to spend " + amount);
        }
        gold -= amount;
    }

    /**
     * Erhöht das Level so lange, wie genügend Erfahrungspunkte vorhanden sind.
     */
    private void applyLevelUps() {
        while (isReadyForNextLevel()) {
            consumeLevelRequirement();
            level++;
        }
    }

    /**
     * {@return das aktuelle Level}
     */
    public int getLevel() {
        return level;
    }

    /**
     * {@return die aktuellen Erfahrungspunkte innerhalb des Levels}
     */
    public int getExperience() {
        return experience;
    }

    /**
     * {@return das verfügbare Gold}
     */
    public int getGold() {
        return gold;
    }

    /**
     * {@return die aktuellen Lebenspunkte}
     */
    public int getHealth() {
        return health;
    }

    /**
     * {@return der aktuelle Titel des Benutzers}
     */
    public String getTitle() {
        return title;
    }

    /**
     * {@return dieser Benutzer selbst (Bequemlichkeitsmethode)}
     */
    public User getUser() {
        return this;
    }

    /**
     * {@return {@code true}, wenn genügend Erfahrung für die nächste Stufe
     * vorhanden ist}
     */
    private boolean isReadyForNextLevel() {
        return experience >= requiredExperienceForCurrentLevel();
    }

    /**
     * {@return die für die aktuelle Stufe benötigte Erfahrung}
     */
    private int requiredExperienceForCurrentLevel() {
        return level * 50;
    }

    /**
     * Zieht die für den Stufenaufstieg benötigte Erfahrung ab.
     */
    private void consumeLevelRequirement() {
        experience -= requiredExperienceForCurrentLevel();
    }

    /**
     * Aktualisiert den Titel passend zum aktuellen Level.
     */
    private void updateTitle() {
        title = resolveTitle(level);
    }

    /**
     * Vermerkt eine Erledigung für den heutigen Tag.
     */
    private void recordCompletionNow() {
        String today = LocalDate.now().toString();
        completionsByDate.put(today, completionsByDate.getOrDefault(today, 0) + 1);
    }

    /**
     * Hängt den aktuellen Erfahrungsstand an den Verlauf an und begrenzt
     * dessen Länge.
     *
     * @param currentExperience der aktuelle Erfahrungsstand
     */
    private void appendXpHistory(int currentExperience) {
        xpHistory.add(currentExperience);
        int maxHistoryEntries = 200;
        if (xpHistory.size() > maxHistoryEntries) {
            xpHistory.remove(0);
        }
    }

    /**
     * Ermittelt den Titel für ein gegebenes Level.
     *
     * @param currentLevel das Level
     * @return der zum Level passende Titel
     */
    private String resolveTitle(int currentLevel) {
        if (currentLevel >= 25) return "Master";
        if (currentLevel >= 20) return "Legend";
        if (currentLevel >= 15) return "Expert";
        if (currentLevel >= 10) return "Apprentice";
        if (currentLevel >= 5) return "Novice";
        return "Beginner";
    }

    /**
     * Gibt alle Aufgaben auf der Konsole aus (Hilfsmittel zur Fehlersuche).
     */
    public void printTasks() {
        tasks.forEach(System.out::println);
    }

    /**
     * {@return die Liste der bereits erledigten Aufgaben}
     */
    public List<AbstractTask> getCompletedTasks() {
        return tasks.stream()
                .filter(AbstractTask::isCompleted)
                .toList();
    }

    /**
     * {@return die Namen aller Aufgaben}
     */
    public List<String> getTaskNames() {
        return tasks.stream()
                .map(AbstractTask::getName)
                .toList();
    }

    /**
     * {@return die Summe der berechneten Punkte aller Aufgaben}
     */
    public int getTotalExperienceFromTasks() {
        return tasks.stream()
                .map(AbstractTask::calculatePoints)
                .reduce(0, Integer::sum);
    }

    /**
     * {@return die Aufgaben absteigend nach Serie sortiert}
     */
    public List<AbstractTask> getTasksSortedByStreak() {
        return tasks.stream()
                .sorted(Comparator.comparingInt(AbstractTask::getStreak).reversed())
                .toList();
    }

    /**
     * Gibt die Anzahl der Erledigungen an einem bestimmten Tag zurück.
     *
     * @param date der Tag
     * @return die Anzahl der Erledigungen an diesem Tag
     */
    public int getCompletionCountForDate(LocalDate date) {
        return completionsByDate.getOrDefault(date.toString(), 0);
    }

    /**
     * {@return eine unveränderbare Momentaufnahme der Erledigungen je Tag}
     */
    public Map<String, Integer> getCompletionsByDateSnapshot() {
        return Collections.unmodifiableMap(new HashMap<>(completionsByDate));
    }

    /**
     * {@return eine unveränderbare Momentaufnahme des XP-Verlaufs}
     */
    public List<Integer> getXpHistorySnapshot() {
        return Collections.unmodifiableList(new ArrayList<>(xpHistory));
    }

    /**
     * Gibt die letzten Einträge des XP-Verlaufs in fester Länge zurück; fehlt
     * Verlauf, wird mit dem aktuellen Stand aufgefüllt.
     *
     * @param size die gewünschte Anzahl an Einträgen
     * @return eine Liste mit genau {@code size} Einträgen
     */
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

    /**
     * Schreibt alle Aufgaben in die Datei {@code tasks.txt} im
     * Benutzerverzeichnis.
     *
     * @throws IOException bei einem Schreibfehler
     */
    public void writeTaskTxt() throws IOException {
        String path = System.getProperty("user.home") + "/tasks.txt";
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

    /**
     * Liest die Aufgaben aus der Datei {@code tasks.txt} und ersetzt die
     * aktuelle Aufgabenliste. Fehlt die Datei, geschieht nichts.
     *
     * @throws IOException bei einem Lesefehler
     */
    public void readTaskTxt() throws IOException {
        String path = System.getProperty("user.home") + "/tasks.txt";
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

    /**
     * Schreibt die Benutzerdaten (Werte, Erledigungen und XP-Verlauf) in die
     * Datei {@code user.txt} im Benutzerverzeichnis.
     *
     * @throws IOException bei einem Schreibfehler
     */
    public void writeUserTxt() throws IOException {
        String path = System.getProperty("user.home") + "/user.txt";
        File file = new File(path);
        file.getParentFile().mkdirs();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            String line = String.format("%s;%d;%d;%d;%d;%s",
                    username, level, experience, gold, health, title);
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

    /**
     * Liest die Benutzerdaten aus der Datei {@code user.txt} und stellt
     * Werte, Erledigungen und XP-Verlauf wieder her.
     *
     * @throws IOException bei einem Lesefehler
     */
    public void readUserTxt() throws IOException {
        String path = System.getProperty("user.home") + "/user.txt";
        File file = new File(path);
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            if (line == null) return;

            String[] parts = line.split(";");
            if (parts.length < 4) return;

            username = parts[0];
            level = Integer.parseInt(parts[1]);
            experience = Integer.parseInt(parts[2]);
            if (parts.length >= 6) {
                gold = Integer.parseInt(parts[3]);
                health = Integer.parseInt(parts[4]);
                title = parts[5];
            } else {
                title = parts[3];
            }

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

    /**
     * Wandelt die Erledigungen je Tag in eine Textzeile um (Format
     * {@code Datum:Anzahl,…}).
     *
     * @return die serialisierten Erledigungen
     */
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

    /**
     * Wandelt den XP-Verlauf in eine durch Kommas getrennte Textzeile um.
     *
     * @return der serialisierte XP-Verlauf
     */
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

    /**
     * Liest die Erledigungen je Tag aus einer Textzeile ein.
     *
     * @param payload die zu lesende Zeile (Format {@code Datum:Anzahl,…})
     */
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

    /**
     * Liest den XP-Verlauf aus einer Textzeile ein.
     *
     * @param payload die zu lesende Zeile (durch Kommas getrennte Werte)
     */
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

    /**
     * Übernimmt einen importierten Zustand (z.&nbsp;B. aus einer Sicherung)
     * vollständig in diesen Benutzer.
     *
     * @param importedUsername          der importierte Benutzername
     * @param importedLevel             das importierte Level
     * @param importedExperience        die importierten Erfahrungspunkte
     * @param importedGold              das importierte Gold
     * @param importedHealth            die importierten Lebenspunkte
     * @param importedTitle             der importierte Titel
     * @param importedTasks             die importierten Aufgaben
     * @param importedCompletionsByDate die importierten Erledigungen je Tag
     * @param importedXpHistory         der importierte XP-Verlauf
     */
    public void applyImportedState(String importedUsername,
                                   int importedLevel,
                                   int importedExperience,
                                   int importedGold,
                                   int importedHealth,
                                   String importedTitle,
                                   List<AbstractTask> importedTasks,
                                   Map<String, Integer> importedCompletionsByDate,
                                   List<Integer> importedXpHistory) {
        this.username = importedUsername;
        this.email = importedUsername.toLowerCase() + "@local";
        this.level = importedLevel;
        this.experience = importedExperience;
        this.gold = importedGold;
        this.health = importedHealth;
        this.title = importedTitle == null || importedTitle.isBlank() ? this.title : importedTitle;

        this.tasks.clear();
        if (importedTasks != null) {
            this.tasks.addAll(importedTasks);
        }

        this.completionsByDate.clear();
        if (importedCompletionsByDate != null) {
            this.completionsByDate.putAll(importedCompletionsByDate);
        }

        this.xpHistory.clear();
        if (importedXpHistory != null && !importedXpHistory.isEmpty()) {
            this.xpHistory.addAll(importedXpHistory);
        } else {
            this.xpHistory.add(this.experience);
        }
    }



    /**
     * {@return eine textuelle Zusammenfassung des Benutzers}
     */
    @Override
    public String toString() {
        return "Username: " + username + "; Level: " + level + "; Exp: " + experience
                + "; Tasks: " + tasks.size() + "; Titel: " + title;
    }

    /**
     * Gibt grundlegende Benutzerdaten auf der Konsole aus (Hilfsmittel zur
     * Fehlersuche).
     */
    public void printUser() {
        System.out.println("User: " + username);
        System.out.println("Titel: " + title);
        System.out.println("Level: " + level);
        System.out.println("Exp: " + experience);
    }
}
