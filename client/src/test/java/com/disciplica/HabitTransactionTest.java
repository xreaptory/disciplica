package com.disciplica;

import model.domain.model.Habit;
import model.domain.model.User;
import model.persistence.DatabaseException;
import model.persistence.SQLiteHabitRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HabitTransactionTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("completeHabit commits completion, xp/gold, and streak atomically")
    void completeHabitCommitsAllChanges() {
        SQLiteHabitRepository repository = createRepository("tx-success.db");
        repository.setTransactionIsolationLevel(java.sql.Connection.TRANSACTION_SERIALIZABLE);

        repository.save(new Habit("Run", "Morning run"));
        Long habitId = repository.getHabitIdByTitleForCurrentUser("Run");
        Long userId = repository.getCurrentUserId();

        int beforeCompletions = repository.getCompletionCountForHabit(habitId);
        int beforeStreak = repository.getHabitStreak(habitId);
        int[] beforeStats = repository.getUserXpAndGold(userId);

        repository.completeHabit(habitId, 1, 15, 3);

        assertEquals(beforeCompletions + 1, repository.getCompletionCountForHabit(habitId));
        assertEquals(beforeStreak + 1, repository.getHabitStreak(habitId));
        int[] afterStats = repository.getUserXpAndGold(userId);
        assertEquals(beforeStats[0] + 15, afterStats[0]);
        assertEquals(beforeStats[1] + 3, afterStats[1]);
    }

    @Test
    @DisplayName("completeHabit rolls back everything when failure occurs mid-transaction")
    void completeHabitRollsBackOnFailure() {
        SQLiteHabitRepository repository = createRepository("tx-rollback.db");
        repository.save(new Habit("Hydrate", "Drink water"));
        Long habitId = repository.getHabitIdByTitleForCurrentUser("Hydrate");
        Long userId = repository.getCurrentUserId();

        int beforeCompletions = repository.getCompletionCountForHabit(habitId);
        int beforeStreak = repository.getHabitStreak(habitId);
        int[] beforeStats = repository.getUserXpAndGold(userId);

        assertThrows(DatabaseException.class,
                () -> repository.completeHabitForRollbackTest(habitId, 1, 20, 5));

        assertEquals(beforeCompletions, repository.getCompletionCountForHabit(habitId));
        assertEquals(beforeStreak, repository.getHabitStreak(habitId));
        int[] afterStats = repository.getUserXpAndGold(userId);
        assertEquals(beforeStats[0], afterStats[0]);
        assertEquals(beforeStats[1], afterStats[1]);
    }

    private SQLiteHabitRepository createRepository(String dbName) {
        Path dbPath = tempDir.resolve(dbName);
        User user = new User("TxUser");
        return new SQLiteHabitRepository(user, "jdbc:sqlite:" + dbPath);
    }
}
