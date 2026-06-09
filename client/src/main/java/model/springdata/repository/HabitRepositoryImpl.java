package model.springdata.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import model.domain.model.Habit;
import model.domain.model.User;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Umsetzung der eigenen Abfragen aus {@link HabitRepositoryCustom}.
 * <p>
 * Verwendet eine native SQL-Abfrage, um Gewohnheiten anhand ihrer
 * Beschreibung zu durchsuchen.
 */
@Repository
public class HabitRepositoryImpl implements HabitRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Durchsucht die Beschreibungen der Gewohnheiten eines Benutzers per
     * nativer SQL-Abfrage (Teilstring-Suche).
     *
     * @param term der Suchbegriff
     * @param user der Benutzer
     * @return die passenden Gewohnheiten, absteigend nach Serie sortiert
     */
    @Override
    public List<Habit> searchByDescriptionNative(String term, User user) {
        return entityManager.createNativeQuery(
                        "SELECT * FROM habits WHERE user_id = :userId AND description LIKE :pattern ORDER BY streak DESC",
                        Habit.class
                )
                .setParameter("userId", user.getId())
                .setParameter("pattern", "%" + term + "%")
                .getResultList();
    }
}
