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

public class HabitQueryLayer {
    private final EntityManager entityManager;

    public HabitQueryLayer(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // JPQL / Named Query: find habits by user + frequency
    public List<Habit> findHabitsByUserAndFrequency(User user, String frequency) {
        return entityManager.createNamedQuery("Habit.findByUserAndFrequency", Habit.class)
                .setParameter("user", user)
                .setParameter("frequency", frequency)
                .getResultList();
    }

    // Criteria API conversion of the same query
    public List<Habit> findHabitsByUserAndFrequencyCriteria(User user, String frequency) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Habit> cq = cb.createQuery(Habit.class);
        Root<Habit> root = cq.from(Habit.class);
        Predicate byUser = cb.equal(root.get("user"), user);
        Predicate byFrequency = cb.equal(root.get("frequency"), frequency);
        cq.select(root).where(cb.and(byUser, byFrequency)).orderBy(cb.asc(root.get("name")));
        return entityManager.createQuery(cq).getResultList();
    }

    // Named query for weekly XP
    public long calculateTotalXpEarnedThisWeek(User user) {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        return entityManager.createNamedQuery("Completion.totalXpForUserSince", Long.class)
                .setParameter("user", user)
                .setParameter("since", since)
                .getSingleResult();
    }

    // Named query for longest streaks
    public List<Habit> findHabitsWithLongestStreaks(User user, int limit) {
        return entityManager.createNamedQuery("Habit.findLongestStreaks", Habit.class)
                .setParameter("user", user)
                .setMaxResults(limit)
                .getResultList();
    }

    // Criteria API with pagination for large lists
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

    // JPQL subtype query example (completions by quality and user)
    public List<Completion> findCompletionsByQuality(User user, int minQuality) {
        return entityManager.createQuery(
                        "select c from Completion c where c.habit.user = :user and c.quality >= :minQuality order by c.completedAt desc",
                        Completion.class)
                .setParameter("user", user)
                .setParameter("minQuality", minQuality)
                .getResultList();
    }
}
