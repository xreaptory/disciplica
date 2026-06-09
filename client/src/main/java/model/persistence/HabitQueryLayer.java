package model.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import model.domain.model.Completion;
import model.domain.model.Habit;
import model.domain.model.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Bündelt fortgeschrittene Datenbankabfragen rund um Gewohnheiten und
 * Erfüllungen.
 * <p>
 * Zeigt verschiedene Abfragetechniken von JPA (benannte JPQL-Abfragen,
 * Criteria-API und Seitenweise-Abfragen) und arbeitet auf einem übergebenen
 * {@link EntityManager}.
 */
public class HabitQueryLayer {
    private final EntityManager entityManager;

    /**
     * Erzeugt die Abfrageschicht mit dem zu verwendenden {@link EntityManager}.
     *
     * @param entityManager der EntityManager für die Abfragen
     */
    public HabitQueryLayer(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Sucht die Gewohnheiten eines Benutzers mit einer bestimmten Häufigkeit
     * (benannte JPQL-Abfrage).
     *
     * @param user      der Benutzer
     * @param frequency die gesuchte Häufigkeit
     * @return die passenden Gewohnheiten
     */
    public List<Habit> findHabitsByUserAndFrequency(User user, String frequency) {
        return entityManager.createNamedQuery("Habit.findByUserAndFrequency", Habit.class)
                .setParameter("user", user)
                .setParameter("frequency", frequency)
                .getResultList();
    }

    /**
     * Wie {@link #findHabitsByUserAndFrequency(User, String)}, jedoch mit der
     * Criteria-API umgesetzt.
     *
     * @param user      der Benutzer
     * @param frequency die gesuchte Häufigkeit
     * @return die passenden Gewohnheiten
     */
    public List<Habit> findHabitsByUserAndFrequencyCriteria(User user, String frequency) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Habit> cq = cb.createQuery(Habit.class);
        Root<Habit> root = cq.from(Habit.class);
        Predicate byUser = cb.equal(root.get("user"), user);
        Predicate byFrequency = cb.equal(root.get("frequency"), frequency);
        cq.select(root).where(cb.and(byUser, byFrequency)).orderBy(cb.asc(root.get("name")));
        return entityManager.createQuery(cq).getResultList();
    }

    /**
     * Berechnet die in den letzten sieben Tagen verdienten Erfahrungspunkte
     * eines Benutzers.
     *
     * @param user der Benutzer
     * @return die Summe der Erfahrungspunkte der letzten Woche
     */
    public long calculateTotalXpEarnedThisWeek(User user) {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        return entityManager.createNamedQuery("Completion.totalXpForUserSince", Long.class)
                .setParameter("user", user)
                .setParameter("since", since)
                .getSingleResult();
    }

    /**
     * Sucht die Gewohnheiten eines Benutzers mit den längsten Serien.
     *
     * @param user  der Benutzer
     * @param limit die maximale Anzahl an Ergebnissen
     * @return die Gewohnheiten mit den längsten Serien
     */
    public List<Habit> findHabitsWithLongestStreaks(User user, int limit) {
        return entityManager.createNamedQuery("Habit.findLongestStreaks", Habit.class)
                .setParameter("user", user)
                .setMaxResults(limit)
                .getResultList();
    }

    /**
     * Liefert eine Seite der Gewohnheiten eines Benutzers (für lange Listen).
     *
     * @param user     der Benutzer
     * @param page     die Seitennummer (beginnend bei 0)
     * @param pageSize die Anzahl der Einträge je Seite
     * @return die Gewohnheiten der angeforderten Seite
     */
    public List<Habit> findHabitsPage(User user, int page, int pageSize) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Habit> cq = cb.createQuery(Habit.class);
        Root<Habit> root = cq.from(Habit.class);
        cq.select(root)
                .where(cb.equal(root.get("user"), user))
                .orderBy(cb.asc(root.get("name")));

        TypedQuery<Habit> query = entityManager.createQuery(cq);
        int safePage = Math.max(page, 0);
        int safePageSize = Math.max(pageSize, 1);
        query.setFirstResult(safePage * safePageSize);
        query.setMaxResults(safePageSize);
        return query.getResultList();
    }

    /**
     * Sucht die Erfüllungen eines Benutzers ab einer Mindestqualität, neueste
     * zuerst.
     *
     * @param user       der Benutzer
     * @param minQuality die geforderte Mindestqualität
     * @return die passenden Erfüllungen
     */
    public List<Completion> findCompletionsByQuality(User user, int minQuality) {
        return entityManager.createQuery(
                        "select c from Completion c where c.habit.user = :user and c.quality >= :minQuality order by c.completedAt desc",
                        Completion.class)
                .setParameter("user", user)
                .setParameter("minQuality", minQuality)
                .getResultList();
    }
}
