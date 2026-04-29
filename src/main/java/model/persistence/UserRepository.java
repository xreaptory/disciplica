package model.persistence;

import model.domain.exception.HabitNotFoundException;
import model.domain.exception.InvalidHabitException;
import model.domain.model.User;
import model.domain.repository.Repository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserRepository implements Repository<User> {

    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);
    private final Path filePath;
    private final String jdbcUrl;

    public UserRepository(String filePath, String dbPath) {
        this.filePath = Paths.get(filePath);
        this.jdbcUrl = "jdbc:sqlite:" + dbPath;
        logger.info("UserRepository initialised: file='{}', db='{}'", filePath, dbPath);
    }

    public void saveUserToFile(User user) throws InvalidHabitException {
        validateUser(user, "Cannot save a null user to file");
        try {
            Files.writeString(filePath, user.toString(), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ioException) {
            throw new UncheckedIOException("Failed to save user to file: " + filePath, ioException);
        }
    }

    public String loadUserFromFile() {
        if (!Files.exists(filePath)) return null;
        return readUserFileOrNull();
    }

    private String readUserFileOrNull() {
        try {
            return Files.readString(filePath);
        } catch (IOException ioException) {
            logLoadFileError(ioException);
            return null;
        }
    }

    private void logLoadFileError(IOException ioException) {
        logger.error("IO error while loading user from file '{}': {}", filePath,
                ioException.getMessage(), ioException);
    }

    public void deleteUserFile() {
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ioException) {
            logger.error("IO error while deleting file '{}': {}", filePath,
                    ioException.getMessage(), ioException);
        }
    }

    public void initDatabase() {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {
            statement.execute(createUsersTableSql());
        } catch (SQLException sqlException) {
            logger.error("SQL error while initialising database: {}", sqlException.getMessage(),
                    sqlException);
        }
    }

    public void saveUserToDb(User user) throws InvalidHabitException {
        validateUser(user, "Cannot save a null user to the database");
        trySaveUser(user);
    }

    private void trySaveUser(User user) {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement preparedStatement = connection
                     .prepareStatement("INSERT OR REPLACE INTO users (username, data) VALUES (?, ?)")) {
            bindUser(preparedStatement, user);
            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            logSaveDbError(user, sqlException);
        }
    }

    private void bindUser(PreparedStatement preparedStatement, User user) throws SQLException {
        preparedStatement.setString(1, user.getUsername());
        preparedStatement.setString(2, user.toString());
    }

    private void logSaveDbError(User user, SQLException sqlException) {
        logger.error("SQL error while saving user '{}' to database: {}", user.getUsername(),
                sqlException.getMessage(), sqlException);
    }

    public String loadUserFromDb(String username) throws HabitNotFoundException {
        try {
            return runLoadUserQuery(username);
        } catch (SQLException sqlException) {
            logLoadDbError(username, sqlException);
            return null;
        }
    }

    private String runLoadUserQuery(String username) throws SQLException, HabitNotFoundException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement preparedStatement = connection
                     .prepareStatement("SELECT data FROM users WHERE username = ?")) {
            preparedStatement.setString(1, username);
            return readUserData(username, preparedStatement);
        }
    }

    private void logLoadDbError(String username, SQLException sqlException) {
        logger.error("SQL error while loading user '{}' from database: {}", username,
                sqlException.getMessage(), sqlException);
    }

    public void deleteUserFromDb(String username) throws HabitNotFoundException {
        try {
            runDeleteUserQuery(username);
        } catch (SQLException sqlException) {
            logDeleteDbError(username, sqlException);
        }
    }

    private void runDeleteUserQuery(String username) throws SQLException, HabitNotFoundException {
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            ensureUserWasDeleted(username, preparedStatement.executeUpdate());
        }
    }

    private void logDeleteDbError(String username, SQLException sqlException) {
        logger.error("SQL error while deleting user '{}': {}", username,
                sqlException.getMessage(), sqlException);
    }

    @Override
    public void save(User entity) throws InvalidHabitException {
        saveUserToDb(entity);
    }

    @Override
    public Optional<User> findByName(String name) {
        try {
            return loadOptionalUser(name);
        } catch (HabitNotFoundException habitNotFoundException) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> findAll() {
        return readAllUsers();
    }

    private List<User> readAllUsers() {
        try {
            return queryAllUsers();
        } catch (SQLException sqlException) {
            logger.error("SQL error in Repository.findAll(): {}", sqlException.getMessage(), sqlException);
            return new ArrayList<>();
        }
    }

    private List<User> queryAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT username FROM users")) {
            collectUsers(users, resultSet);
        }
        return users;
    }

    @Override
    public void delete(String name) throws HabitNotFoundException {
        deleteUserFromDb(name);
    }

    private void validateUser(User user, String message) throws InvalidHabitException {
        if (user == null) {
            throw new InvalidHabitException(message);
        }
    }

    private String createUsersTableSql() {
        return "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT NOT NULL UNIQUE,"
                + "data TEXT NOT NULL"
                + ");";
    }

    private String readUserData(String username, PreparedStatement preparedStatement)
            throws SQLException, HabitNotFoundException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            if (!resultSet.next()) {
                throw new HabitNotFoundException("User not found in database: " + username);
            }
            return resultSet.getString("data");
        }
    }

    private void ensureUserWasDeleted(String username, int deletedRows) throws HabitNotFoundException {
        if (deletedRows == 0) {
            throw new HabitNotFoundException("Cannot delete - user not found in database: " + username);
        }
    }

    private Optional<User> loadOptionalUser(String username) throws HabitNotFoundException {
        String data = loadUserFromDb(username);
        if (data == null) {
            return Optional.empty();
        }
        return Optional.of(new User(username));
    }

    private void collectUsers(List<User> users, ResultSet resultSet) throws SQLException {
        while (resultSet.next()) {
            users.add(new User(resultSet.getString("username")));
        }
    }
}
