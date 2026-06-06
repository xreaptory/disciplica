package com.disciplica;

import model.domain.model.Habit;
import model.domain.model.User;
import model.persistence.SQLiteHabitRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SQLiteAdvancedFeaturesTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("FTS search, ranking, explain plan, and backup are available")
    void advancedDatabaseFeaturesWork() throws Exception {
        Path dbPath = tempDir.resolve("advanced.db");
        SQLiteHabitRepository repository = new SQLiteHabitRepository(new User("AdvancedUser"), "jdbc:sqlite:" + dbPath);

        repository.save(new Habit("Morning Run", "Run 5km in the park"));
        repository.save(new Habit("Read", "Read database optimization notes"));

        Long runId = repository.getHabitIdByTitleForCurrentUser("Morning Run");
        repository.completeHabit(runId, 1, 10, 2);

        List<SQLiteHabitRepository.HabitSearchResult> searchResults = repository.searchHabitsByDescription("database", new User("AdvancedUser"));
        assertFalse(searchResults.isEmpty());

        List<SQLiteHabitRepository.HabitCompletionRank> ranked = repository.rankHabitsByCompletionRate(new User("AdvancedUser"));
        assertFalse(ranked.isEmpty());

        List<String> plan = repository.explainQueryPlan("SELECT * FROM habits WHERE user_id = 1");
        assertFalse(plan.isEmpty());

        Path backupTarget = tempDir.resolve("backup").resolve("advanced-backup.db");
        repository.backupDatabase(backupTarget);
        assertTrue(Files.exists(backupTarget));
        assertTrue(Files.size(backupTarget) > 0);
    }
}
