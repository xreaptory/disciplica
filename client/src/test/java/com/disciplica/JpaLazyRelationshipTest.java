package com.disciplica;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import model.domain.model.Completion;
import model.domain.model.Habit;
import model.domain.model.User;
import model.persistence.JpaEntityManagerHelper;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JpaLazyRelationshipTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("User -> habits and Habit -> completions are lazy-loaded")
    void lazyRelationshipsLoadOnAccess() {
        Path dbPath = tempDir.resolve("jpa-lazy-test.db");
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("jakarta.persistence.jdbc.url", "jdbc:sqlite:" + dbPath);
        overrides.put("hibernate.hbm2ddl.auto", "update");

        EntityManagerFactory emf = JpaEntityManagerHelper.createEntityManagerFactory(overrides);
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        User user = new User("LazyUser");
        Habit habit = new Habit("Stretch", "Morning stretch");
        Completion completion = new Completion(LocalDateTime.now(), 1);
        habit.addCompletion(completion);
        user.addHabit(habit);
        em.persist(user);
        em.getTransaction().commit();

        Long userId = user.getId();
        em.clear();

        User loaded = em.find(User.class, userId);
        assertFalse(Hibernate.isInitialized(loaded.getHabits()));
        int habitsSize = loaded.getHabits().size();
        assertTrue(habitsSize > 0);
        assertTrue(Hibernate.isInitialized(loaded.getHabits()));

        Habit loadedHabit = loaded.getHabits().get(0);
        assertFalse(Hibernate.isInitialized(loadedHabit.getCompletions()));
        int completionsSize = loadedHabit.getCompletions().size();
        assertTrue(completionsSize > 0);
        assertTrue(Hibernate.isInitialized(loadedHabit.getCompletions()));

        em.close();
        JpaEntityManagerHelper.close();
    }
}
