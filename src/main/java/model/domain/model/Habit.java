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

    protected Habit() {
        this.name = "";
        this.description = "";
        this.completed = false;
        this.streak = 0;
        this.difficulty = "medium";
        this.frequency = "daily";
        this.metadataJson = "{}";
    }

    public Habit(String name, String description) {
        logger.debug("Creating Habit: name='{}', description='{}'", name, description);
        try {
            initializeHabit(name, description);
        } catch (InvalidHabitException invalidHabitException) {
            throw toIllegalArgument(name, invalidHabitException);
        }
    }

    private void initializeHabit(String name, String description) throws InvalidHabitException {
        setName(name);
        setDescription(description);
        this.difficulty = "medium";
        this.frequency = "daily";
        this.metadataJson = "{}";
        logger.info("Habit created successfully: '{}'", name);
    }

    private IllegalArgumentException toIllegalArgument(String name,
            InvalidHabitException invalidHabitException) {
        logger.error("Failed to create Habit with name='{}': {}", name,
                invalidHabitException.getMessage(), invalidHabitException);
        return new IllegalArgumentException("Invalid habit data: " + invalidHabitException.getMessage(),
                invalidHabitException);
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }

    public void setName(String name) throws InvalidHabitException {
        logger.debug("Setting name for Habit, new value='{}'", name);
        if (name == null || name.isBlank()) {
            logger.error("Habit name must not be null or blank");
            throw new InvalidHabitException("Habit name must not be null or blank");
        }
        this.name = name;
        logger.debug("Habit name set to '{}'", name);
    }

    public String getDescription() {
        return description;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        if (frequency != null && !frequency.isBlank()) {
            this.frequency = frequency;
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<Completion> getCompletions() {
        return completions;
    }

    public void addCompletion(Completion completion) {
        completions.add(completion);
        completion.setHabit(this);
    }

    public void setDescription(String description) throws InvalidHabitException {
        logger.debug("Setting description for Habit '{}', new value='{}'", name, description);
        if (description == null) {
            logger.error("Habit description must not be null for habit '{}'", name);
            throw new InvalidHabitException("Habit description must not be null");
        }
        this.description = description;
        logger.debug("Habit '{}' description set successfully", name);
    }

    @Override
    public int getProgress() {
        int progress = completed ? 100 : 0;
        logger.debug("getProgress() for Habit '{}': {}", name, progress);
        return progress;
    }

    @Override
    public int getStreak() {
        logger.debug("getStreak() for Habit '{}': {}", name, streak);
        return streak;
    }

    public boolean isCompleted() {
        return completed;
    }

    @Override
    public boolean complete() {
        logger.debug("Attempting to complete Habit '{}'", name);
        if (completed) return reportAlreadyCompleted();
        markCompleted();
        return true;
    }

    private boolean reportAlreadyCompleted() {
        logger.warn("Habit already completed: {}", name);
        return false;
    }

    private void markCompleted() {
        completed = true;
        streak++;
        logger.info("Habit completed: '{}', new streak={}", name, streak);
    }

    @Override
    public Reward getReward() {
        logger.debug("Getting reward for Habit '{}', streak={}", name, streak);
        int rewardThreshold = 10 + (streak * 5);
        Reward reward = new Reward("Habit Streak", "Reward for completing " + name, rewardThreshold);
        logger.info("Reward generated for Habit '{}': {}", name, reward);
        return reward;
    }

    public void resetStreak() {
        logger.info("Resetting streak for Habit '{}', was {}", name, streak);
        if (vacationFreezeArmed) {
            vacationFreezeArmed = false;
            logger.info("Vacation freeze consumed for Habit '{}'; streak remains {}", name, streak);
            return;
        }
        streak = 0;
        logger.debug("Streak reset complete for Habit '{}'", name);
    }

    public void enableVacationFreeze() {
        vacationFreezeArmed = true;
        logger.info("Vacation freeze armed for Habit '{}'", name);
    }

    public void print() {
        logger.debug("print() called for Habit '{}'", name);
        System.out.println("Habit: " + name);
    }

    @Override
    public String toString() {
        return "Name: " + name + "; Description: " + description + "; isCompleted: " + completed + "; Streak: " + streak;
    }
}




