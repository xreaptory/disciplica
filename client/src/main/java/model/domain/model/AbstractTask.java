package model.domain.model;

import model.domain.contract.Trackable;
import model.domain.exception.InvalidHabitException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Gemeinsame Oberklasse aller Aufgaben und Gewohnheiten.
 * <p>
 * Hält die grundlegenden Eigenschaften (Name, Beschreibung, Punkte und
 * Erledigt-Status), prüft die Gültigkeit der Werte und stellt das Verhalten
 * zum Abschließen bereit. Konkrete Unterklassen legen über
 * {@link #calculatePoints()} fest, wie sich der Punktewert berechnet.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = DailyHabit.class, name = "DailyHabit"),
    @JsonSubTypes.Type(value = WeeklyHabit.class, name = "WeeklyHabit"),
    @JsonSubTypes.Type(value = OneTimeTask.class, name = "OneTimeTask")
})
@MappedSuperclass
public abstract class AbstractTask implements Trackable {
    private static final Logger logger = LoggerFactory.getLogger(AbstractTask.class);
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "description", nullable = false)
    private String description;
    private boolean isCompleted;
    @Column(name = "points", nullable = false)
    private int points;

    /**
     * Erzeugt eine leere Aufgabe mit Standardwerten (wird vor allem von der
     * Persistenzschicht benötigt).
     */
    protected AbstractTask() {
        this.name = "";
        this.description = "";
        this.points = 0;
        this.isCompleted = false;
    }

    /**
     * Erzeugt eine Aufgabe und prüft dabei die übergebenen Werte.
     *
     * @param name        der Name der Aufgabe (darf nicht leer sein)
     * @param description die Beschreibung (darf nicht {@code null} sein)
     * @param points      der Punktewert (darf nicht negativ sein)
     * @throws InvalidHabitException wenn einer der Werte ungültig ist
     */
    public AbstractTask(String name, String description, int points) throws InvalidHabitException {
        validateName(name);
        validateDescription(description, name);
        validatePoints(points, name);
        this.name = name;
        this.description = description;
        this.points = points;
        isCompleted = false;
    }

    /**
     * Prüft, dass der Name nicht leer ist.
     *
     * @param candidateName der zu prüfende Name
     * @throws InvalidHabitException wenn der Name leer ist
     */
    private void validateName(String candidateName) throws InvalidHabitException {
        if (candidateName == null || candidateName.isBlank()) {
            logger.error("Task name must not be null or blank");
            throw new InvalidHabitException("Task name must not be null or blank");
        }
    }

    /**
     * Prüft, dass die Beschreibung nicht {@code null} ist.
     *
     * @param candidateDescription die zu prüfende Beschreibung
     * @param taskName             der Name der Aufgabe (für die Fehlermeldung)
     * @throws InvalidHabitException wenn die Beschreibung {@code null} ist
     */
    private void validateDescription(String candidateDescription, String taskName)
            throws InvalidHabitException {
        if (candidateDescription == null) {
            logger.error("Task description must not be null for task '{}'", taskName);
            throw new InvalidHabitException("Task description must not be null");
        }
    }

    /**
     * Prüft, dass der Punktewert nicht negativ ist.
     *
     * @param candidatePoints der zu prüfende Punktewert
     * @param taskName        der Name der Aufgabe (für die Fehlermeldung)
     * @throws InvalidHabitException wenn der Punktewert negativ ist
     */
    private void validatePoints(int candidatePoints, String taskName) throws InvalidHabitException {
        if (candidatePoints < 0) {
            throw new InvalidHabitException("Task points must be non-negative, got: " + candidatePoints);
        }
    }

    /**
     * {@return der Name der Aufgabe}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@return die Beschreibung der Aufgabe}
     */
    public String getDescription() {
        return description;
    }

    /**
     * {@return die Datenbank-Kennung der Aufgabe oder {@code null}, wenn noch
     * nicht gespeichert}
     */
    public Long getId() {
        return id;
    }

    /**
     * {@return {@code true}, wenn die Aufgabe abgeschlossen ist}
     */
    public boolean isCompleted() {
        return isCompleted;
    }

    /**
     * Bean-Getter für das gespeicherte Feld „completed“ (von Jackson unter
     * genau diesem Namen erwartet).
     *
     * @return {@code true}, wenn die Aufgabe abgeschlossen ist
     */
    public boolean getCompleted() {
        return isCompleted;
    }

    /**
     * {@return der Punktewert der Aufgabe}
     */
    public int getPoints() {
        return points;
    }

    /**
     * Schließt die Aufgabe ab, sofern sie noch nicht abgeschlossen ist.
     *
     * @return {@code true}, wenn die Aufgabe durch diesen Aufruf abgeschlossen
     *         wurde; {@code false}, wenn sie bereits abgeschlossen war
     */
    public boolean complete() {
        logger.debug("complete() called on '{}' ({})", name, this.getClass().getSimpleName());
        if (!isCompleted) {
            isCompleted = true;
            logger.info("Task completed: '{}' ({})", name, this.getClass().getSimpleName());
            return true;
        }
        logger.warn("Attempted to complete already-completed task: '{}' ({})", name, this.getClass().getSimpleName());
        return false;
    }

    /**
     * Zwei Aufgaben gelten als gleich, wenn Name, Beschreibung und Punkte
     * übereinstimmen.
     *
     * @param o das zu vergleichende Objekt
     * @return {@code true}, wenn die Aufgaben inhaltlich gleich sind
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractTask that = (AbstractTask) o;
        return points == that.points &&
                name.equals(that.name) &&
                description.equals(that.description);
    }

    /**
     * {@return ein Hashwert passend zu {@link #equals(Object)}}
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, description, points);
    }

    /**
     * Berechnet den effektiven Punktewert der Aufgabe. Die konkrete Berechnung
     * legen die Unterklassen fest.
     *
     * @return der berechnete Punktewert
     */
    public abstract int calculatePoints();

    /**
     * Setzt den Erledigt-Status der Aufgabe.
     *
     * @param completed {@code true}, wenn die Aufgabe als abgeschlossen gelten
     *                  soll
     */
    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
    }

    /**
     * Gibt den Fortschritt in Prozent zurück: 100, wenn abgeschlossen, sonst 0.
     *
     * @return 100 bei Abschluss, sonst 0
     */
    @JsonIgnore
    @Override
    public int getProgress() {
        return isCompleted ? 100 : 0;
    }

    /**
     * Eine einfache Aufgabe führt keine Serie; gibt daher immer 0 zurück.
     *
     * @return immer 0
     */
    @Override
    public int getStreak() {
        return 0;
    }

    /**
     * {@return eine textuelle Darstellung der Aufgabe im Format
     * {@code Name;Beschreibung;Punkte;Erledigt}}
     */
    @Override
    public String toString() {
        return name+";"+description+";"+points+";"+isCompleted;
    }
}
