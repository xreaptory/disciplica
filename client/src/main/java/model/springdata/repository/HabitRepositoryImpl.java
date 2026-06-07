package model.springdata.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import model.domain.model.Habit;
import model.domain.model.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class HabitRepositoryImpl implements HabitRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

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
