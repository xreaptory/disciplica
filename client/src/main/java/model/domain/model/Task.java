package model.domain.model;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Persistente Oberklasse aller konkreten Aufgabentypen.
 * <p>
 * Bildet die gemeinsame Datenbanktabelle {@code tasks} ab; die einzelnen
 * Unterklassen werden über eine Diskriminator-Spalte unterschieden.
 */
@Entity
@Table(name = "tasks")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "task_type", discriminatorType = DiscriminatorType.STRING, length = 32)
public abstract class Task extends AbstractTask {

    @Transient
    private String unusedPlaceholder;

    /**
     * Erzeugt eine leere Aufgabe (wird von der Persistenzschicht benötigt).
     */
    protected Task() {
        super();
    }

    /**
     * Erzeugt eine Aufgabe mit Name, Beschreibung und Punkten.
     *
     * @param name        der Name der Aufgabe
     * @param description die Beschreibung der Aufgabe
     * @param points      der Punktewert der Aufgabe
     * @throws model.domain.exception.InvalidHabitException wenn einer der Werte
     *         ungültig ist
     */
    protected Task(String name, String description, int points) throws model.domain.exception.InvalidHabitException {
        super(name, description, points);
    }
}
