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

/**
 * Eine einzelne Erfüllung einer {@link Habit Gewohnheit} zu einem bestimmten
 * Zeitpunkt.
 * <p>
 * Hält fest, wann die Gewohnheit erfüllt wurde, mit welcher Qualität und
 * wie viele Erfahrungspunkte dabei verdient wurden. Bildet die Tabelle
 * {@code completions} ab.
 */
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

    /**
     * Erzeugt eine leere Erfüllung (wird von der Persistenzschicht benötigt).
     */
    protected Completion() {
    }

    /**
     * Erzeugt eine Erfüllung ohne verdiente Erfahrungspunkte.
     *
     * @param completedAt Zeitpunkt der Erfüllung
     * @param quality     Qualität der Erfüllung
     */
    public Completion(LocalDateTime completedAt, int quality) {
        this.completedAt = completedAt;
        this.quality = quality;
        this.xpEarned = 0;
    }

    /**
     * Erzeugt eine Erfüllung mit verdienten Erfahrungspunkten.
     *
     * @param completedAt Zeitpunkt der Erfüllung
     * @param quality     Qualität der Erfüllung
     * @param xpEarned    verdiente Erfahrungspunkte
     */
    public Completion(LocalDateTime completedAt, int quality, int xpEarned) {
        this.completedAt = completedAt;
        this.quality = quality;
        this.xpEarned = xpEarned;
    }

    /**
     * {@return die Datenbank-Kennung der Erfüllung}
     */
    public Long getId() {
        return id;
    }

    /**
     * {@return die Gewohnheit, zu der diese Erfüllung gehört}
     */
    public Habit getHabit() {
        return habit;
    }

    /**
     * Ordnet die Erfüllung einer Gewohnheit zu.
     *
     * @param habit die zugehörige Gewohnheit
     */
    public void setHabit(Habit habit) {
        this.habit = habit;
    }

    /**
     * {@return der Zeitpunkt der Erfüllung}
     */
    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    /**
     * {@return die Qualität der Erfüllung}
     */
    public int getQuality() {
        return quality;
    }

    /**
     * {@return die bei dieser Erfüllung verdienten Erfahrungspunkte}
     */
    public int getXpEarned() {
        return xpEarned;
    }
}
