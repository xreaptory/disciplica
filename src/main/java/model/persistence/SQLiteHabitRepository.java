package model.persistence;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import model.domain.model.Habit;
import model.domain.model.User;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Singleton
public class SQLiteHabitRepository implements HabitRepository {
    private static final String DEFAULT_DB_PATH = "data/habittracker.db";

    private final DatabaseConnection databaseConnection;
    private final User currentUser;
    private final String jdbcUrl;
    private final ConcurrentMap<String, Long> userIdCache = new ConcurrentHashMap<>();
    private int transactionIsolationLevel = Connection.TRANSACTION_SERIALIZABLE;

    @Inject
    public SQLiteHabitRepository(User currentUser) {
        this(currentUser, "jdbc:sqlite:" + DEFAULT_DB_PATH);
    }

    public SQLiteHabitRepository(User currentUser, String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        this.databaseConnection = new DatabaseConnection(jdbcUrl);
        this.currentUser = currentUser;
        ensureSchema();
    }

    @Override
    public void save(Habit habit) {
        String sql = "INSERT INTO habits (user_id, title, description, difficulty, frequency, metadata_json) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindHabitToInsertStatement(statement, habit, resolveUserId(currentUser));
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new DatabaseException("Failed to save habit", exception);
        }
    }

    @Override
    public Optional<Habit> findById(Long id) {
        String sql = "SELECT id, user_id, title, description, difficulty, frequency, metadata_json FROM habits WHERE id = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapHabitRow(resultSet));
            }
        } catch (SQLException exception) {
            throw new DatabaseException("Failed to find habit by id", exception);
        }
    }

    @Override
    public List<Habit> findByUser(User user) {
        String sql = "SELECT id, user_id, title, description, difficulty, frequency, metadata_json FROM habits WHERE user_id = ? ORDER BY id";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, resolveUserId(user));
            try (ResultSet resultSet = statement.executeQuery()) {
                return mapHabitList(resultSet);
            }
        } catch (SQLException exception) {
            throw new DatabaseException("Failed to find habits by user", exception);
        }
    }

    @Override
    public void update(Habit habit) {
        Long habitId = findHabitIdByTitleAndUser(habit.getName(), currentUser);
        if (habitId == null) {
            throw new DatabaseException("Cannot update habit that does not exist");
        }
        String sql = "UPDATE habits SET title = ?, description = ?, difficulty = ?, frequency = ?, metadata_json = ? WHERE id = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindHabitToUpdateStatement(statement, habit, habitId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new DatabaseException("Failed to update habit", exception);
        }
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM habits WHERE id = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new DatabaseException("Failed to delete habit", exception);
        }
    }

    @Override
    public List<Habit> findAll() {
        String sql = "SELECT id, user_id, title, description, difficulty, frequency, metadata_json FROM habits ORDER BY id";
        try (Connection connection = databaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            return mapHabitList(resultSet);
        } catch (SQLException exception) {
            throw new DatabaseException("Failed to find all habits", exception);
        }
    }

    @Override
    public void completeHabit(Long habitId, int quality, int xpGain, int goldGain) {
        executeCompleteHabitTransaction(habitId, quality, xpGain, goldGain, false);
    }

    public void completeHabitForRollbackTest(Long habitId, int quality, int xpGain, int goldGain) {
        executeCompleteHabitTransaction(habitId, quality, xpGain, goldGain, true);
    }

    public void setTransactionIsolationLevel(int isolationLevel) {
        this.transactionIsolationLevel = isolationLevel;
    }

    public List<HabitSearchResult> searchHabitsByDescription(String searchTerm, User user) {
        String sql = """
                SELECT h.id, h.title, h.description, bm25(habits_fts) AS score
                FROM habits_fts
                JOIN habits h ON h.id = habits_fts.rowid
                WHERE habits_fts MATCH ? AND h.user_id = ?
                ORDER BY score ASC
                """;
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, searchTerm);
            statement.setLong(2, resolveUserId(user));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<HabitSearchResult> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(new HabitSearchResult(
                            resultSet.getLong("id"),
                            resultSet.getString("title"),
                            resultSet.getString("description"),
                            resultSet.getDouble("score")
                    ));
                }
                return results;
            }
        } catch (SQLException exception) {
            throw new DatabaseException("Failed full-text habit search", exception);
        }
    }

    public List<HabitCompletionRank> rankHabitsByCompletionRate(User user) {
        String sql = """
                WITH per_habit AS (
                    SELECT
                        h.id AS habit_id,
                        h.title AS title,
                        COUNT(c.id) AS completions,
                        CAST(COUNT(c.id) AS REAL) / NULLIF((julianday('now') - julianday(MIN(DATE(c.completed_at))) + 1), 0) AS completion_rate
                    FROM habits h
                    LEFT JOIN completions c ON c.habit_id = h.id
                    WHERE h.user_id = ?
                    GROUP BY h.id, h.title
                )
                SELECT
                    habit_id,
                    title,
                    completions,
                    COALESCE(completion_rate, 0.0) AS completion_rate,
                    DENSE_RANK() OVER (ORDER BY COALESCE(completion_rate, 0.0) DESC, completions DESC) AS rank_position
                FROM per_habit
                ORDER BY rank_position, title
                """;
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, resolveUserId(user));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<HabitCompletionRank> ranking = new ArrayList<>();
                while (resultSet.next()) {
                    ranking.add(new HabitCompletionRank(
                            resultSet.getLong("habit_id"),
                            resultSet.getString("title"),
                            resultSet.getInt("completions"),
                            resultSet.getDouble("completion_rate"),
                            resultSet.getInt("rank_position")
                    ));
                }
                return ranking;
            }
        } catch (SQLException exception) {
            throw new DatabaseException("Failed to rank habits by completion rate", exception);
        }
    }

    public List<String> explainQueryPlan(String sql) {
        String planSql = "EXPLAIN QUERY PLAN " + sql;
        try (Connection connection = databaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(planSql)) {
            List<String> lines = new ArrayList<>();
            while (resultSet.next()) {
                lines.add(resultSet.getString("detail"));
            }
            return lines;
        } catch (SQLException exception) {
            throw new DatabaseException("Failed to explain query plan", exception);
        }
    }

    public Path backupDatabase(Path targetFile) {
        try {
            String dbFile = jdbcUrl.replaceFirst("^jdbc:sqlite:", "");
            Path source = Path.of(dbFile);
            if (!Files.exists(source)) {
                throw new DatabaseException("Database file not found: " + source);
            }
            Files.createDirectories(targetFile.getParent());
            Files.copy(source, targetFile, StandardCopyOption.REPLACE_EXISTING);
            return targetFile;
        } catch (Exception exception) {
            throw new DatabaseException("Failed to backup database", exception);
        }
    }

    public Optional<HabitWithCompletions> findHabitWithCompletions(Long habitId) {
        return findHabitWithCompletions(habitId, 200, 0);
    }

    public Optional<HabitWithCompletions> findHabitWithCompletions(Long habitId, int pageSize, int offset) {
        Optional<Habit> habitOptional = findById(habitId);
        if (habitOptional.isEmpty()) {
            return Optional.empty();
        }
        List<CompletionRecord> completions = loadCompletions(habitId, pageSize, offset);
        return Optional.of(new HabitWithCompletions(habitId, habitOptional.get(), completions));
    }

    private List<CompletionRecord> loadCompletions(Long habitId, int pageSize, int offset) {
        String sql = "SELECT id, habit_id, completed_at, quality FROM completions WHERE habit_id = ? ORDER BY completed_at DESC LIMIT ? OFFSET ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, habitId);
            statement.setInt(2, Math.max(1, pageSize));
            statement.setInt(3, Math.max(0, offset));
            try (ResultSet resultSet = statement.executeQuery()) {
                List<CompletionRecord> completions = new ArrayList<>();
                while (resultSet.next()) {
                    completions.add(new CompletionRecord(
                            resultSet.getLong("id"),
                            resultSet.getLong("habit_id"),
                            resultSet.getString("completed_at"),
                            resultSet.getInt("quality")
                    ));
                }
                return completions;
            }
        } catch (SQLException exception) {
            throw new DatabaseException("Failed to load completions for habit", exception);
        }
    }

    private List<Habit> mapHabitList(ResultSet resultSet) throws SQLException {
        List<Habit> habits = new ArrayList<>();
        while (resultSet.next()) {
            habits.add(mapHabitRow(resultSet));
        }
        return habits;
    }

    private Habit mapHabitRow(ResultSet resultSet) throws SQLException {
        String title = resultSet.getString("title");
        String description = resultSet.getString("description");
        return new Habit(title, description);
    }

    private void bindHabitToInsertStatement(PreparedStatement statement, Habit habit, long userId) throws SQLException {
        statement.setLong(1, userId);
        statement.setString(2, habit.getName());
        statement.setString(3, habit.getDescription());
        statement.setString(4, habit.getDifficulty());
        statement.setString(5, habit.getFrequency());
        statement.setString(6, "{}");
    }

    private void bindHabitToUpdateStatement(PreparedStatement statement, Habit habit, long habitId) throws SQLException {
        statement.setString(1, habit.getName());
        statement.setString(2, habit.getDescription());
        statement.setString(3, habit.getDifficulty());
        statement.setString(4, habit.getFrequency());
        statement.setString(5, "{}");
        statement.setLong(6, habitId);
    }

    private long resolveUserId(User user) throws SQLException {
        Long cachedUserId = userIdCache.get(user.getUsername());
        if (cachedUserId != null) {
            return cachedUserId;
        }

        String selectSql = "SELECT id FROM users WHERE username = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement select = connection.prepareStatement(selectSql)) {
            select.setString(1, user.getUsername());
            try (ResultSet resultSet = select.executeQuery()) {
                if (resultSet.next()) {
                    long userId = resultSet.getLong("id");
                    userIdCache.putIfAbsent(user.getUsername(), userId);
                    return userId;
                }
            }
        }
        String insertSql = "INSERT INTO users (username, email, level, xp, health, gold) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement insert = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            insert.setString(1, user.getUsername());
            insert.setString(2, user.getUsername().toLowerCase() + "@local");
            insert.setInt(3, user.getLevel());
            insert.setInt(4, user.getExperience());
            insert.setInt(5, 50);
            insert.setInt(6, 0);
            insert.executeUpdate();
            try (ResultSet keys = insert.getGeneratedKeys()) {
                if (keys.next()) {
                    long userId = keys.getLong(1);
                    userIdCache.putIfAbsent(user.getUsername(), userId);
                    return userId;
                }
            }
        }
        throw new SQLException("Unable to resolve user id");
    }

    private Long findHabitIdByTitleAndUser(String title, User user) {
        String sql = "SELECT id FROM habits WHERE title = ? AND user_id = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            statement.setLong(2, resolveUserId(user));
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("id");
                }
                return null;
            }
        } catch (SQLException exception) {
            throw new DatabaseException("Failed to resolve habit id", exception);
        }
    }

    private void ensureSchema() {
        String usersSql = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT NOT NULL UNIQUE,"
                + "email TEXT NOT NULL UNIQUE,"
                + "level INTEGER NOT NULL DEFAULT 1,"
                + "xp INTEGER NOT NULL DEFAULT 0,"
                + "health INTEGER NOT NULL DEFAULT 50,"
                + "gold INTEGER NOT NULL DEFAULT 0"
                + ")";
        String habitsSql = "CREATE TABLE IF NOT EXISTS habits ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "user_id INTEGER NOT NULL,"
                + "title TEXT NOT NULL,"
                + "description TEXT NOT NULL DEFAULT '',"
                + "difficulty TEXT NOT NULL,"
                + "frequency TEXT NOT NULL,"
                + "streak INTEGER NOT NULL DEFAULT 0,"
                + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,"
                + "UNIQUE (user_id, title)"
                + ")";
        String completionsSql = "CREATE TABLE IF NOT EXISTS completions ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "habit_id INTEGER NOT NULL,"
                + "completed_at TEXT NOT NULL DEFAULT (datetime('now')),"
                + "quality INTEGER NOT NULL,"
                + "FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE"
                + ")";
        try (Connection connection = databaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(usersSql);
            statement.execute(habitsSql);
            statement.execute(completionsSql);
            statement.execute("CREATE INDEX IF NOT EXISTS idx_habits_user_id ON habits(user_id)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_habits_user_frequency ON habits(user_id, frequency)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_habits_user_streak ON habits(user_id, streak DESC)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_completions_habit_id ON completions(habit_id)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_completions_habit_completed_at ON completions(habit_id, completed_at DESC)");
            statement.execute("CREATE INDEX IF NOT EXISTS idx_users_username ON users(username)");
            ensureStreakColumn(statement);
            ensureMetadataJsonColumn(statement);
            ensureFullTextSearch(statement);
        } catch (SQLException exception) {
            throw new DatabaseException("Failed to ensure habit schema", exception);
        }
    }

    private void executeCompleteHabitTransaction(Long habitId, int quality, int xpGain, int goldGain, boolean forceFailure) {
        validateQuality(quality);
        String insertCompletionSql = "INSERT INTO completions (habit_id, completed_at, quality, xp_earned) VALUES (?, datetime('now'), ?, ?)";
        String updateUserSql = "UPDATE users SET xp = xp + ?, gold = gold + ? WHERE id = ?";
        String updateStreakSql = "UPDATE habits SET streak = streak + 1 WHERE id = ?";

        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            connection.setTransactionIsolation(transactionIsolationLevel);

            long userId = findUserIdForHabit(habitId, connection);
            if (userId <= 0) {
                throw new DatabaseException("Cannot complete habit: habit/user not found");
            }

            try (PreparedStatement insertCompletion = connection.prepareStatement(insertCompletionSql);
                 PreparedStatement updateUser = connection.prepareStatement(updateUserSql);
                 PreparedStatement updateStreak = connection.prepareStatement(updateStreakSql)) {

                insertCompletion.setLong(1, habitId);
                insertCompletion.setInt(2, quality);
                insertCompletion.setInt(3, xpGain);
                insertCompletion.executeUpdate();

                if (forceFailure) {
                    throw new SQLException("Forced failure during transaction");
                }

                updateUser.setInt(1, xpGain);
                updateUser.setInt(2, goldGain);
                updateUser.setLong(3, userId);
                updateUser.executeUpdate();

                updateStreak.setLong(1, habitId);
                updateStreak.executeUpdate();

                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (Exception exception) {
            throw new DatabaseException("Failed to complete habit transaction", exception);
        }
    }

    private long findUserIdForHabit(Long habitId, Connection connection) throws SQLException {
        String sql = "SELECT user_id FROM habits WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, habitId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("user_id");
                }
                return -1;
            }
        }
    }

    private void validateQuality(int quality) {
        if (quality < -1 || quality > 1) {
            throw new DatabaseException("Quality must be between -1 and 1");
        }
    }

    private void ensureStreakColumn(Statement statement) throws SQLException {
        try {
            statement.execute("ALTER TABLE habits ADD COLUMN streak INTEGER NOT NULL DEFAULT 0");
        } catch (SQLException exception) {
            String message = exception.getMessage() == null ? "" : exception.getMessage().toLowerCase();
            if (!message.contains("duplicate column name")) {
                throw exception;
            }
        }
    }

    private void ensureMetadataJsonColumn(Statement statement) throws SQLException {
        try {
            statement.execute("ALTER TABLE habits ADD COLUMN metadata_json TEXT NOT NULL DEFAULT '{}'");
        } catch (SQLException exception) {
            String message = exception.getMessage() == null ? "" : exception.getMessage().toLowerCase();
            if (!message.contains("duplicate column name")) {
                throw exception;
            }
        }
    }

    private void ensureFullTextSearch(Statement statement) throws SQLException {
        statement.execute("""
                CREATE VIRTUAL TABLE IF NOT EXISTS habits_fts USING fts5(
                    title,
                    description,
                    content='habits',
                    content_rowid='id'
                )
                """);
        statement.execute("INSERT OR REPLACE INTO habits_fts(rowid, title, description) SELECT id, title, description FROM habits");
        statement.execute("""
                CREATE TRIGGER IF NOT EXISTS habits_ai AFTER INSERT ON habits BEGIN
                    INSERT INTO habits_fts(rowid, title, description) VALUES (new.id, new.title, new.description);
                END
                """);
        statement.execute("""
                CREATE TRIGGER IF NOT EXISTS habits_ad AFTER DELETE ON habits BEGIN
                    INSERT INTO habits_fts(habits_fts, rowid, title, description) VALUES ('delete', old.id, old.title, old.description);
                END
                """);
        statement.execute("""
                CREATE TRIGGER IF NOT EXISTS habits_au AFTER UPDATE ON habits BEGIN
                    INSERT INTO habits_fts(habits_fts, rowid, title, description) VALUES ('delete', old.id, old.title, old.description);
                    INSERT INTO habits_fts(rowid, title, description) VALUES (new.id, new.title, new.description);
                END
                """);
    }

    public int getCompletionCountForHabit(Long habitId) {
        String sql = "SELECT COUNT(*) AS cnt FROM completions WHERE habit_id = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, habitId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt("cnt") : 0;
            }
        } catch (SQLException exception) {
            throw new DatabaseException("Failed to read completion count", exception);
        }
    }

    public int getHabitStreak(Long habitId) {
        String sql = "SELECT streak FROM habits WHERE id = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, habitId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt("streak") : 0;
            }
        } catch (SQLException exception) {
            throw new DatabaseException("Failed to read habit streak", exception);
        }
    }

    public int[] getUserXpAndGold(Long userId) {
        String sql = "SELECT xp, gold FROM users WHERE id = ?";
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return new int[]{0, 0};
                }
                return new int[]{resultSet.getInt("xp"), resultSet.getInt("gold")};
            }
        } catch (SQLException exception) {
            throw new DatabaseException("Failed to read user xp/gold", exception);
        }
    }

    public Long getHabitIdByTitleForCurrentUser(String title) {
        return findHabitIdByTitleAndUser(title, currentUser);
    }

    public Long getCurrentUserId() {
        try {
            return resolveUserId(currentUser);
        } catch (SQLException exception) {
            throw new DatabaseException("Failed to resolve current user id", exception);
        }
    }

    public record CompletionRecord(Long id, Long habitId, String completedAt, Integer quality) {
    }

    public record HabitWithCompletions(Long id, Habit habit, List<CompletionRecord> completions) {
    }

    public record HabitSearchResult(Long habitId, String title, String description, Double score) {
    }

    public record HabitCompletionRank(Long habitId, String title, Integer completionCount,
                                     Double completionRate, Integer rankPosition) {
    }
}
