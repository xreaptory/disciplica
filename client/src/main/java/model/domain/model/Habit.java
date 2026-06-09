package model.domain.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.EntityListeners;
import model.domain.contract.Completable;
import model.domain.contract.Trackable;
import model.domain.exception.InvalidHabitException;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Eine Gewohnheit, die ein Benutzer wiederholt erfüllt.
 * <p>
 * Eine Gewohnheit führt eine Serie (Streak) aufeinanderfolgender Erfüllungen,
 * gewährt beim Abschließen eine {@link Reward Belohnung} und kann über einen
 * „Urlaubs-Freeze“ vor dem Zurücksetzen der Serie geschützt werden. Die
 * Klasse ist zugleich eine JPA-Entität und bildet die Tabelle {@code habits}
 * ab.
 */
@Entity
@Table(name = "habits")
@EntityListeners(AuditingEntityListener.class)
@NamedQueries({
        @NamedQuery(
                name = "Habit.findByUserAndFrequency",
                query = "select h from Habit h where h.user = :user and h.frequency = :frequency order by h.name"
        ),
        @NamedQuery(
                name = "Habit.findLongestStreaks",
                query = "select h from Habit h where h.user = :user order by h.streak desc, h.name asc"
        )
})
public class Habit implements Completable, Trackable {
    private static final Logger logger = LoggerFactory.getLogger(Habit.class);
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "title", nullable = false)
    private String name;
    @Column(name = "description", nullable = false)
    private String description;
    @Column(name = "difficulty", nullable = false)
    private String difficulty;
    @Column(name = "frequency", nullable = false)
    private String frequency;
    @Column(name = "metadata_json", nullable = false)
    private String metadataJson;
    @Column(name = "completed", nullable = false)
    private boolean completed;
    @Column(name = "streak", nullable = false)
    private int streak;
    private boolean vacationFreezeArmed;
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @OneToMany(mappedBy = "habit", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Completion> completions = new ArrayList<>();

    /**
     * Erzeugt eine leere Gewohnheit mit Standardwerten (wird von der
     * Persistenzschicht benötigt).
     */
    protected Habit() {
        this.name = "";
        this.description = "";
        this.completed = false;
        this.streak = 0;
        this.difficulty = "medium";
        this.frequency = "daily";
        this.metadataJson = "{}";
    }

    /**
     * Erzeugt eine neue Gewohnheit mit Name und Beschreibung.
     *
     * @param name        der Name der Gewohnheit (darf nicht leer sein)
     * @param description die Beschreibung (darf nicht {@code null} sein)
     * @throws IllegalArgumentException wenn die übergebenen Daten ungültig sind
     */
    public Habit(String name, String description) {
        logger.debug("Creating Habit: name='{}', description='{}'", name, description);
        try {
            initializeHabit(name, description);
        } catch (InvalidHabitException invalidHabitException) {
            throw toIllegalArgument(name, invalidHabitException);
        }
    }

    /**
     * Setzt die Anfangswerte der Gewohnheit und prüft Name und Beschreibung.
     *
     * @param name        der Name der Gewohnheit
     * @param description die Beschreibung
     * @throws InvalidHabitException wenn Name oder Beschreibung ungültig sind
     */
    private void initializeHabit(String name, String description) throws InvalidHabitException {
        setName(name);
        setDescription(description);
        this.difficulty = "medium";
        this.frequency = "daily";
        this.metadataJson = "{}";
        logger.info("Habit created successfully: '{}'", name);
    }

    /**
     * Wandelt eine {@link InvalidHabitException} in eine
     * {@link IllegalArgumentException} um (für den Konstruktor).
     *
     * @param name                   der Name der Gewohnheit
     * @param invalidHabitException  der ursprüngliche Fehler
     * @return die umgewandelte Laufzeit-Ausnahme
     */
    private IllegalArgumentException toIllegalArgument(String name,
            InvalidHabitException invalidHabitException) {
        logger.error("Failed to create Habit with name='{}': {}", name,
                invalidHabitException.getMessage(), invalidHabitException);
        return new IllegalArgumentException("Invalid habit data: " + invalidHabitException.getMessage(),
                invalidHabitException);
    }

    /**
     * {@return der Name der Gewohnheit}
     */
    public String getName() {
        return name;
    }

    /**
     * {@return die Datenbank-Kennung oder {@code null}, wenn noch nicht
     * gespeichert}
     */
    public Long getId() {
        return id;
    }

    /**
     * Setzt den Namen der Gewohnheit.
     *
     * @param name der neue Name (darf nicht leer sein)
     * @throws InvalidHabitException wenn der Name leer ist
     */
    public void setName(String name) throws InvalidHabitException {
        logger.debug("Setting name for Habit, new value='{}'", name);
        if (name == null || name.isBlank()) {
            logger.error("Habit name must not be null or blank");
            throw new InvalidHabitException("Habit name must not be null or blank");
        }
        this.name = name;
        logger.debug("Habit name set to '{}'", name);
    }

    /**
     * {@return die Beschreibung der Gewohnheit}
     */
    public String getDescription() {
        return description;
    }

    /**
     * {@return der Schwierigkeitsgrad der Gewohnheit}
     */
    public String getDifficulty() {
        return difficulty;
    }

    /**
     * {@return die Häufigkeit der Gewohnheit (z.&nbsp;B. täglich)}
     */
    public String getFrequency() {
        return frequency;
    }

    /**
     * Setzt die Häufigkeit der Gewohnheit (leere Werte werden ignoriert).
     *
     * @param frequency die neue Häufigkeit
     */
    public void setFrequency(String frequency) {
        if (frequency != null && !frequency.isBlank()) {
            this.frequency = frequency;
        }
    }

    /**
     * {@return der Benutzer, dem diese Gewohnheit gehört}
     */
    public User getUser() {
        return user;
    }

    /**
     * Ordnet die Gewohnheit einem Benutzer zu.
     *
     * @param user der Besitzer der Gewohnheit
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * {@return die Liste der einzelnen Erfüllungen dieser Gewohnheit}
     */
    public List<Completion> getCompletions() {
        return completions;
    }

    /**
     * Fügt eine Erfüllung hinzu und verknüpft sie mit dieser Gewohnheit.
     *
     * @param completion die hinzuzufügende Erfüllung
     */
    public void addCompletion(Completion completion) {
        completions.add(completion);
        completion.setHabit(this);
    }

    /**
     * Setzt die Beschreibung der Gewohnheit.
     *
     * @param description die neue Beschreibung (darf nicht {@code null} sein)
     * @throws InvalidHabitException wenn die Beschreibung {@code null} ist
     */
    public void setDescription(String description) throws InvalidHabitException {
        logger.debug("Setting description for Habit '{}', new value='{}'", name, description);
        if (description == null) {
            logger.error("Habit description must not be null for habit '{}'", name);
            throw new InvalidHabitException("Habit description must not be null");
        }
        this.description = description;
        logger.debug("Habit '{}' description set successfully", name);
    }

    /**
     * Gibt den Fortschritt in Prozent zurück: 100, wenn erfüllt, sonst 0.
     *
     * @return 100 bei Erfüllung, sonst 0
     */
    @Override
    public int getProgress() {
        int progress = completed ? 100 : 0;
        logger.debug("getProgress() for Habit '{}': {}", name, progress);
        return progress;
    }

    /**
     * {@return die aktuelle Serie aufeinanderfolgender Erfüllungen}
     */
    @Override
    public int getStreak() {
        logger.debug("getStreak() for Habit '{}': {}", name, streak);
        return streak;
    }

    /**
     * {@return {@code true}, wenn die Gewohnheit aktuell als erfüllt gilt}
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Erfüllt die Gewohnheit und erhöht die Serie um eins, sofern sie noch
     * nicht erfüllt ist.
     *
     * @return {@code true}, wenn die Gewohnheit durch diesen Aufruf erfüllt
     *         wurde; {@code false}, wenn sie bereits erfüllt war
     */
    @Override
    public boolean complete() {
        logger.debug("Attempting to complete Habit '{}'", name);
        if (completed) return reportAlreadyCompleted();
        markCompleted();
        return true;
    }

    /**
     * Protokolliert, dass die Gewohnheit bereits erfüllt war.
     *
     * @return immer {@code false}
     */
    private boolean reportAlreadyCompleted() {
        logger.warn("Habit already completed: {}", name);
        return false;
    }

    /**
     * Markiert die Gewohnheit als erfüllt und erhöht die Serie.
     */
    private void markCompleted() {
        completed = true;
        streak++;
        logger.info("Habit completed: '{}', new streak={}", name, streak);
    }

    /**
     * Erzeugt die Belohnung für das Erfüllen dieser Gewohnheit; ihr Wert
     * steigt mit der Länge der Serie.
     *
     * @return die zugehörige Belohnung
     */
    @Override
    public Reward getReward() {
        logger.debug("Getting reward for Habit '{}', streak={}", name, streak);
        int rewardThreshold = 10 + (streak * 5);
        Reward reward = new Reward("Habit Streak", "Reward for completing " + name, rewardThreshold);
        logger.info("Reward generated for Habit '{}': {}", name, reward);
        return reward;
    }

    /**
     * Setzt die Serie zurück. Ist ein Urlaubs-Freeze aktiv, wird dieser
     * stattdessen verbraucht und die Serie bleibt erhalten.
     */
    public void resetStreak() {
        logger.info("Resetting streak for Habit '{}', was {}", name, streak);
        if (consumeVacationFreezeIfArmed()) {
            return;
        }
        streak = 0;
        logger.debug("Streak reset complete for Habit '{}'", name);
    }

    /**
     * Aktiviert den Urlaubs-Freeze: Das nächste Zurücksetzen der Serie wird
     * einmalig verhindert.
     */
    public void enableVacationFreeze() {
        vacationFreezeArmed = true;
        logger.info("Vacation freeze armed for Habit '{}'", name);
    }

    /**
     * Verbraucht einen aktiven Urlaubs-Freeze, falls vorhanden.
     *
     * @return {@code true}, wenn ein Freeze verbraucht wurde und die Serie
     *         erhalten bleibt
     */
    private boolean consumeVacationFreezeIfArmed() {
        if (!vacationFreezeArmed) {
            return false;
        }
        vacationFreezeArmed = false;
        logger.info("Vacation freeze consumed for Habit '{}'; streak remains {}", name, streak);
        return true;
    }

    /**
     * Gibt den Namen der Gewohnheit auf der Konsole aus (Hilfsmittel zur
     * Fehlersuche).
     */
    public void print() {
        logger.debug("print() called for Habit '{}'", name);
        System.out.println("Habit: " + name);
    }

    /**
     * {@return eine textuelle Darstellung der Gewohnheit mit Name,
     * Beschreibung, Erledigt-Status und Serie}
     */
    @Override
    public String toString() {
        return "Name: " + name + "; Description: " + description + "; isCompleted: " + completed + "; Streak: " + streak;
    }
}
