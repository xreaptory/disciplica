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
 * Eine wöchentlich zu erfüllende Aufgabe.
 * <p>
 * Funktioniert wie eine {@link DailyHabit}, vergibt jedoch einen höheren
 * Serienbonus (15 Punkte je Serienwoche).
 */
@Entity
@Table(name = "weekly_habits")
@DiscriminatorValue("WEEKLY")
public class WeeklyHabit extends Task {
    private static final Logger logger = LoggerFactory.getLogger(WeeklyHabit.class);
    @JsonProperty("streak")
    private int streak;

    /**
     * Erzeugt eine wöchentliche Gewohnheit aus einem gespeicherten Zustand und
     * stellt Serie und Erledigt-Status wieder her.
     *
     * @param state der gespeicherte Zustand
     * @throws InvalidHabitException wenn die enthaltenen Daten ungültig sind
     */
    @JsonCreator
    public WeeklyHabit(WeeklyHabitState state) throws InvalidHabitException {
        super(state.name(), state.description(), state.points());
        streak = state.streak();
        restoreCompletedState(state.completed());
        logger.debug("WeeklyHabit created: name='{}', points={}, streak={}, completed={}", state.name(),
                state.points(), streak, state.completed());
    }

    /**
     * Erzeugt eine neue wöchentliche Gewohnheit mit Serie 0.
     *
     * @param name        der Name
     * @param description die Beschreibung
     * @param points      der Grund-Punktewert
     * @throws InvalidHabitException wenn einer der Werte ungültig ist
     */
    public WeeklyHabit(String name, String description, int points) throws InvalidHabitException {
        super(name,description,points);
        streak = 0;
    }

    /**
     * Erzeugt eine leere wöchentliche Gewohnheit (für die Persistenzschicht).
     */
    protected WeeklyHabit() {
        super();
    }

    /**
     * Stellt beim Laden den Erledigt-Status wieder her.
     *
     * @param completed {@code true}, wenn als erledigt gespeichert
     */
    private void restoreCompletedState(boolean completed) {
        if (completed) {
            super.complete();
        }
    }

    /**
     * {@return die aktuelle Serie aufeinanderfolgender Wochen}
     */
    @Override
    @JsonProperty("streak")
    public int getStreak() {
        return streak;
    }

    /**
     * Setzt die Serie auf 0 zurück.
     */
    public void resetStreak() {
        logger.info("Resetting streak for WeeklyHabit '{}', was {}", getName(), streak);
        streak = 0;
        logger.debug("Streak reset complete for '{}'", getName());
    }

    /**
     * Erfüllt die Gewohnheit und erhöht bei erstmaligem Abschluss die Serie.
     *
     * @return {@code true}, wenn durch diesen Aufruf erfüllt wurde
     */
    @Override
    public boolean complete() {
        logger.debug("Attempting to complete WeeklyHabit '{}'", getName());
        boolean completedNow = super.complete();
        updateStreakAfterCompletion(completedNow);
        return completedNow;
    }

    /**
     * Erhöht die Serie, falls die Gewohnheit soeben erfüllt wurde.
     *
     * @param completedNow {@code true}, wenn gerade erfüllt
     */
    private void updateStreakAfterCompletion(boolean completedNow) {
        if (completedNow) {
            streak++;
            logger.info("WeeklyHabit '{}' completed. New streak: {}", getName(), streak);
            return;
        }
        logger.warn("WeeklyHabit '{}' was already completed, streak unchanged", getName());
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
     * Berechnet die Punkte als Grundwert zuzüglich eines Serienbonus
     * (15 Punkte je Serienwoche).
     *
     * @return der berechnete Punktewert
     */
    @Override
    public int calculatePoints() {
        int total = super.getPoints() + (streak * 15);
        logger.debug("Calculated points for WeeklyHabit '{}': base={} + streakBonus={} = {}",
                getName(), super.getPoints(), streak * 15, total);
        return total;
    }

    /**
     * {@return eine textuelle Darstellung der Gewohnheit mit Typkürzel „W“}
     */
    @Override
    public String toString() {
        return "W;"+super.toString() +";"+streak;
    }

    /**
     * Gespeicherter Zustand einer wöchentlichen Gewohnheit für JSON.
     *
     * @param name        der Name
     * @param description die Beschreibung
     * @param points      der Grund-Punktewert
     * @param streak      die Serie
     * @param completed   der Erledigt-Status
     */
    public record WeeklyHabitState(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("points") int points,
            @JsonProperty("streak") int streak,
            @JsonProperty("completed") boolean completed) {
    }
}
