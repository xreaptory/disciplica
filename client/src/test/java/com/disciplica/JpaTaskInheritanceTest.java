package com.disciplica;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import model.domain.model.DailyHabit;
import model.domain.model.OneTimeTask;
import model.domain.model.Task;
import model.domain.model.WeeklyHabit;
import model.persistence.JpaEntityManagerHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JpaTaskInheritanceTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("JOINED inheritance persists and queries polymorphic Task types")
    void taskInheritancePolymorphismWorks() throws Exception {
        Path dbPath = tempDir.resolve("jpa-task-inheritance.db");
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("jakarta.persistence.jdbc.url", "jdbc:sqlite:" + dbPath);
        overrides.put("hibernate.hbm2ddl.auto", "update");

        EntityManagerFactory emf = JpaEntityManagerHelper.createEntityManagerFactory(overrides);
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.persist(new DailyHabit("Run", "Morning run", 10));
        em.persist(new WeeklyHabit("Clean", "Weekend cleaning", 20));
        em.persist(new OneTimeTask("Buy milk", "Get groceries", 5));
        em.getTransaction().commit();
        em.clear();

        List<Task> tasks = em.createQuery("select t from Task t", Task.class).getResultList();
        assertEquals(3, tasks.size());

        List<Task> dailyViaType = em.createQuery(
                "select t from Task t where type(t) = DailyHabit", Task.class).getResultList();
        assertEquals(1, dailyViaType.size());
        assertTrue(dailyViaType.get(0) instanceof DailyHabit);

        List<DailyHabit> dailyViaTreat = em.createQuery(
                "select treat(t as DailyHabit) from Task t where type(t) = DailyHabit", DailyHabit.class)
                .getResultList();
        assertEquals(1, dailyViaTreat.size());
        assertEquals("Run", dailyViaTreat.get(0).getName());

        em.close();
        JpaEntityManagerHelper.close();
    }
}
