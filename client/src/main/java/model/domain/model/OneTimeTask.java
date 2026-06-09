package model.domain.model;

import model.domain.exception.InvalidHabitException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Eine einmalige Aufgabe (To-do), die nach dem Abschließen erledigt bleibt
 * und keine Serie führt.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "one_time_tasks")
@DiscriminatorValue("ONE_TIME")
public class OneTimeTask extends Task {
    private static final Logger logger = LoggerFactory.getLogger(OneTimeTask.class);

    /**
     * Erzeugt eine einmalige Aufgabe aus einem gespeicherten Zustand und
     * stellt den Erledigt-Status wieder her.
     *
     * @param state der gespeicherte Zustand
     * @throws InvalidHabitException wenn die enthaltenen Daten ungültig sind
     */
    @JsonCreator
    public OneTimeTask(OneTimeTaskState state) throws InvalidHabitException {
        super(state.name(), state.description(), state.points());
        restoreCompletedState(state.completed());
        logger.debug("OneTimeTask created: name='{}', points={}, completed={}", state.name(),
                state.points(), state.completed());
    }

    /**
     * Erzeugt eine neue einmalige Aufgabe.
     *
     * @param name        der Name
     * @param description die Beschreibung
     * @param points      der Punktewert
     * @throws InvalidHabitException wenn einer der Werte ungültig ist
     */
    public OneTimeTask(String name, String description, int points) throws InvalidHabitException {
        super(name, description, points);
    }

    /**
     * Erzeugt eine leere einmalige Aufgabe (für die Persistenzschicht).
     */
    protected OneTimeTask() {
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
     * {@return eine textuelle Darstellung der Aufgabe mit Typkürzel „O“}
     */
    public String toString(){
        return "O;"+getName()+";"+getDescription()+";"+getPoints()+";"+isCompleted();
    }

    /**
     * Eine einmalige Aufgabe vergibt genau ihren Grund-Punktewert.
     *
     * @return der Punktewert der Aufgabe
     */
    @Override
    public int calculatePoints() {
        int awardedPoints = super.getPoints();
        logger.debug("Calculated points for OneTimeTask '{}': {}", getName(), awardedPoints);
        return awardedPoints;
    }

    /**
     * Eine einmalige Aufgabe führt keine Serie; gibt daher immer 0 zurück.
     *
     * @return immer 0
     */
    @Override
    @JsonIgnore
    public int getStreak() {
        return 0;
    }

    /**
     * Gespeicherter Zustand einer einmaligen Aufgabe für JSON.
     *
     * @param name        der Name
     * @param description die Beschreibung
     * @param points      der Punktewert
     * @param completed   der Erledigt-Status
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OneTimeTaskState(
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("points") int points,
            @JsonProperty("completed") boolean completed) {
    }

}
