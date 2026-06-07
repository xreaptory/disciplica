package model.domain.model;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "tasks")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "task_type", discriminatorType = DiscriminatorType.STRING, length = 32)
public abstract class Task extends AbstractTask {

    @Transient
    private String unusedPlaceholder;

    protected Task() {
        super();
    }

    protected Task(String name, String description, int points) throws model.domain.exception.InvalidHabitException {
        super(name, description, points);
    }
}
