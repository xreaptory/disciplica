package model.domain.model;

import model.domain.exception.InvalidHabitException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Eine täglich zu erfüllende Aufgabe.
 * <p>
 * Führt eine Serie aufeinanderfolgender Tage und vergibt beim Berechnen der
 * Punkte einen Serienbonus.
 */
@Entity
@Table(name = "daily_habits")
@DiscriminatorValue("DAILY")
public class DailyHabit extends Task {
    private static final Logger logger = LoggerFactory.getLogger(DailyHabit.class);
    @JsonProperty("streak")
    private int streak;

    /**
     * Erzeugt eine Daily aus einem gespeicherten Zustand (z.&nbsp;B. beim
     * Laden aus JSON) und stellt Serie und Erledigt-Status wieder her.
     *
     * @param state der gespeicherte Zustand
     * @throws InvalidHabitException wenn die enthaltenen Daten ungültig sind
     */
    @JsonCreator
    public DailyHabit(DailyHabitState state) throws InvalidHabitException {
        super(state.name(), state.description(), state.points());
        streak = state.streak();
        restoreCompletedState(state.completed());
        logger.debug("DailyHabit created: name='{}', points={}, streak={}, completed={}", state.name(),
                state.points(), streak, state.completed());
    }

    /**
     * Erzeugt eine neue Daily mit Serie 0.
     *
     * @param name        der Name
     * @param description die Beschreibung
     * @param points      der Grund-Punktewert
     * @throws InvalidHabitException wenn einer der Werte ungültig ist
     */
    public DailyHabit(String name, String description, int points) throws InvalidHabitException {
        super(name,description,points);
        streak = 0;

    }

    /**
     * Erzeugt eine leere Daily (wird von der Persistenzschicht benötigt).
     */
    protected DailyHabit() {
        super();
    }

    /**
     * Stellt beim Laden den Erledigt-Status wieder her.
     *
     * @param completed {@code true}, wenn die Daily als erledigt gespeichert
     *                  war
     */
    private void restoreCompletedState(boolean completed) {
        if (completed) {
            super.complete();
        }
    }

    /**
     * {@return die aktuelle Serie aufeinanderfolgender Tage}
     */
    @Override
    @JsonProperty("streak")
    public int getStreak() {
        return streak;
    }

    /**
     * Setzt die Serie.
     *
     * @param streak die neue Serie (darf nicht negativ sein)
     * @throws IllegalArgumentException wenn der Wert negativ ist
     */
    public void setStreak(int streak) {
        if(streak < 0) {
            logger.warn("Attempted to set a negative streak value for DailyHabit '{}'", getName());
            throw new IllegalArgumentException("Streak cannot be negative");
        }
        logger.info("Setting streak for DailyHabit '{}', was {}, now {}", getName(), this.streak, streak);
        this.streak = streak;
    }

    /**
     * Setzt die Serie auf 0 zurück.
     */
    public void resetStreak() {
        logger.info("Resetting streak for DailyHabit '{}', was {}", getName(), streak);
        streak = 0;
        logger.debug("Streak reset complete for '{}'", getName());
    }

    /**
     * Erfüllt die Daily und erhöht bei erstmaligem Abschluss die Serie.
     *
     * @return {@code true}, wenn die Daily durch diesen Aufruf erfüllt wurde
     */
    @Override
    public boolean complete() {
        logger.debug("Attempting to complete DailyHabit '{}'", getName());
        boolean completedNow = super.complete();
        updateStreakAfterCompletion(completedNow);
        return completedNow;
    }

    /**
     * Erhöht die Serie, falls die Daily soeben erfüllt wurde.
     *
     * @param completedNow {@code true}, wenn die Daily gerade erfüllt wurde
     */
    private void updateStreakAfterCompletion(boolean completedNow) {
        if (completedNow) {
            streak++;
            logger.info("DailyHabit '{}' completed. New streak: {}", getName(), streak);
            return;
        }
        logger.warn("DailyHabit '{}' was already completed, streak unchanged", getName());
    }

    /**
     * Berechnet die Punkte als Grundwert zuzüglich eines Serienbonus
     * (5 Punkte je Serientag).
     *
     * @return der berechnete Punktewert
     */
    @Override
    public int calculatePoints() {
        int total = super.getPoints() + (streak * 5);
        logger.debug("Calculated points for DailyHabit '{}': base={} + streakBonus={} = {}",
                getName(), super.getPoints(), streak * 5, total);
        return total;
    }

    /**
     * {@return eine textuelle Darstellung der Daily mit Typkürzel „D“}
     */
    @Override
    public String toString() {
        return "D;"+super.toString() +";"+streak;
    }

    /**
     * Gespeicherter Zustand einer Daily zum Auslesen aus bzw. Schreiben in
     * JSON.
     *
     * @param name        der Name
     * @param description die Beschreibung
     * @param points      der Grund-Punktewert
     * @param streak      die Serie
     * @param completed   der Erledigt-Status
     */
    public record DailyHabitState(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("points") int points,
            @JsonProperty("streak") int streak,
            @JsonProperty("completed") boolean completed) {
    }
}
