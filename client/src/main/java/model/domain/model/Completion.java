package model.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "completions")
@NamedQuery(
        name = "Completion.totalXpForUserSince",
        query = "select coalesce(sum(c.xpEarned), 0) from Completion c where c.habit.user = :user and c.completedAt >= :since"
)
public class Completion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "habit_id", nullable = false)
    private Habit habit;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @Column(name = "quality", nullable = false)
    private int quality;

    @Column(name = "xp_earned", nullable = false)
    private int xpEarned;

    protected Completion() {
    }

    public Completion(LocalDateTime completedAt, int quality) {
        this.completedAt = completedAt;
        this.quality = quality;
        this.xpEarned = 0;
    }

    public Completion(LocalDateTime completedAt, int quality, int xpEarned) {
        this.completedAt = completedAt;
        this.quality = quality;
        this.xpEarned = xpEarned;
    }

    public Long getId() {
        return id;
    }

    public Habit getHabit() {
        return habit;
    }

    public void setHabit(Habit habit) {
        this.habit = habit;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public int getQuality() {
        return quality;
    }

    public int getXpEarned() {
        return xpEarned;
    }
}
