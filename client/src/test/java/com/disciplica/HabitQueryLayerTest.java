package com.disciplica;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import model.domain.model.Completion;
import model.domain.model.Habit;
import model.domain.model.User;
import model.persistence.HabitQueryLayer;
import model.persistence.JpaEntityManagerHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HabitQueryLayerTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("JPQL, named queries, Criteria API, and pagination work for habits")
    void queryLayerOperationsWork() {
        Path dbPath = tempDir.resolve("query-layer.db");
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("jakarta.persistence.jdbc.url", "jdbc:sqlite:" + dbPath);
        overrides.put("hibernate.hbm2ddl.auto", "update");

        EntityManagerFactory emf = JpaEntityManagerHelper.createEntityManagerFactory(overrides);
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        User user = new User("QueryUser");

        Habit h1 = new Habit("Run", "Morning run");
        h1.setFrequency("daily");
        h1.addCompletion(new Completion(LocalDateTime.now().minusDays(1), 1, 20));

        Habit h2 = new Habit("Read", "Read docs");
        h2.setFrequency("weekly");
        h2.addCompletion(new Completion(LocalDateTime.now().minusDays(2), 1, 10));

        Habit h3 = new Habit("Stretch", "Evening stretch");
        h3.setFrequency("daily");

        user.addHabit(h1);
        user.addHabit(h2);
        user.addHabit(h3);
        em.persist(user);
        em.getTransaction().commit();
        em.clear();

        User loadedUser = em.createQuery("select u from User u where u.username = :username", User.class)
                .setParameter("username", "QueryUser")
                .getSingleResult();

        HabitQueryLayer queryLayer = new HabitQueryLayer(em);

        List<Habit> dailyNamed = queryLayer.findHabitsByUserAndFrequency(loadedUser, "daily");
        assertFalse(dailyNamed.isEmpty());

        List<Habit> dailyCriteria = queryLayer.findHabitsByUserAndFrequencyCriteria(loadedUser, "daily");
        assertFalse(dailyCriteria.isEmpty());

        long weeklyXp = queryLayer.calculateTotalXpEarnedThisWeek(loadedUser);
        assertTrue(weeklyXp >= 30);

        List<Habit> longest = queryLayer.findHabitsWithLongestStreaks(loadedUser, 2);
        assertFalse(longest.isEmpty());

        List<Habit> page = queryLayer.findHabitsPage(loadedUser, 0, 2);
        assertTrue(page.size() <= 2);

        em.close();
        JpaEntityManagerHelper.close();
    }
}
