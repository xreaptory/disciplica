package model.domain.model;

import model.domain.contract.Trackable;
import model.domain.exception.InvalidHabitException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

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
public abstract class AbstractTask implements Trackable {
    private static final Logger logger = LoggerFactory.getLogger(AbstractTask.class);
    private final String name;
    private final String description;
    private boolean isCompleted;
    private final int points;

    public AbstractTask(String name, String description, int points) throws InvalidHabitException {
        validateName(name);
        validateDescription(description, name);
        validatePoints(points, name);
        this.name = name;
        this.description = description;
        this.points = points;
        isCompleted = false;
    }

    private void validateName(String candidateName) throws InvalidHabitException {
        if (candidateName == null || candidateName.isBlank()) {
            logger.error("Task name must not be null or blank");
            throw new InvalidHabitException("Task name must not be null or blank");
        }
    }

    private void validateDescription(String candidateDescription, String taskName)
            throws InvalidHabitException {
        if (candidateDescription == null) {
            logger.error("Task description must not be null for task '{}'", taskName);
            throw new InvalidHabitException("Task description must not be null");
        }
    }

    private void validatePoints(int candidatePoints, String taskName) throws InvalidHabitException {
        if (candidatePoints < 0) {
            throw new InvalidHabitException("Task points must be non-negative, got: " + candidatePoints);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    // Jackson expects this exact bean getter name for the persisted "completed" field.
    public boolean getCompleted() {
        return isCompleted;
    }

    public int getPoints() {
        return points;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractTask that = (AbstractTask) o;
        return points == that.points &&
                name.equals(that.name) &&
                description.equals(that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, points);
    }

    public abstract int calculatePoints();

    @JsonIgnore
    @Override
    public int getProgress() {
        return isCompleted ? 100 : 0;
    }

    @Override
    public int getStreak() {
        return 0;
    }

    @Override
    public String toString() {
        return name+";"+description+";"+points+";"+isCompleted;
    }
}


