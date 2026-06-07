package com.disciplica;

import com.disciplica.testtags.UnitTest;
import model.domain.model.Habit;
import model.domain.model.User;
import model.persistence.SQLiteHabitRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
class PerformanceRegressionTest {

    @Test
    @DisplayName("Optimized user-id lookup path must not regress beyond 10%")
    void optimizedPathMustStayWithinTenPercent() throws Exception {
        User user = new User("perf-user");
        Path dbFile = Files.createTempFile("disciplica-perf-", ".db");
        String jdbcUrl = "jdbc:sqlite:" + dbFile.toAbsolutePath();
        SQLiteHabitRepository repository = new SQLiteHabitRepository(user, jdbcUrl);

        for (int i = 0; i < 1000; i++) {
            repository.save(new Habit("habit-" + i, "desc-" + i));
        }

        long baselineNanos = measureBaselineQuery(jdbcUrl, user.getUsername(), 250);
        long optimizedNanos = measureOptimizedRepositoryQuery(repository, user, 250);

        double allowed = baselineNanos * 1.10d;
        assertTrue(optimizedNanos <= allowed,
                "Performance regression detected. optimizedNanos=" + optimizedNanos
                        + ", baselineNanos=" + baselineNanos
                        + ", allowedMax=" + (long) allowed);
    }

    private long measureBaselineQuery(String jdbcUrl, String username, int loops) throws Exception {
        long start = System.nanoTime();
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement userStmt = connection.prepareStatement("SELECT id FROM users WHERE username = ?");
             PreparedStatement habitStmt = connection.prepareStatement("SELECT id FROM habits WHERE user_id = ?")) {
            for (int i = 0; i < loops; i++) {
                userStmt.setString(1, username);
                long userId;
                try (ResultSet userRs = userStmt.executeQuery()) {
                    userId = userRs.next() ? userRs.getLong("id") : -1;
                }
                habitStmt.setLong(1, userId);
                try (ResultSet habitsRs = habitStmt.executeQuery()) {
                    while (habitsRs.next()) {
                        habitsRs.getLong("id");
                    }
                }
            }
        }
        return System.nanoTime() - start;
    }

    private long measureOptimizedRepositoryQuery(SQLiteHabitRepository repository, User user, int loops) {
        // Warm up cache path.
        repository.findByUser(user);

        long start = System.nanoTime();
        for (int i = 0; i < loops; i++) {
            repository.findByUser(user);
        }
        return System.nanoTime() - start;
    }
}
