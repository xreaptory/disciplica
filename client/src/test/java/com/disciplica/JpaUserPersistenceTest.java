package com.disciplica;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import model.domain.model.User;
import model.persistence.JpaEntityManagerHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JpaUserPersistenceTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Persist and retrieve User by generated JPA ID")
    void persistAndFindUserById() {
        Path dbPath = tempDir.resolve("jpa-user-test.db");
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("jakarta.persistence.jdbc.url", "jdbc:sqlite:" + dbPath);
        overrides.put("hibernate.hbm2ddl.auto", "update");

        EntityManagerFactory emf = JpaEntityManagerHelper.createEntityManagerFactory(overrides);
        EntityManager em = emf.createEntityManager();

        User user = new User("JpaTester");
        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();

        Long persistedId = user.getId();
        assertNotNull(persistedId);

        User loaded = em.find(User.class, persistedId);
        assertNotNull(loaded);
        assertTrue("JpaTester".equals(loaded.getUsername()));

        em.close();
        JpaEntityManagerHelper.close();
    }
}
