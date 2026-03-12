package com.disciplica;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles persistence of User data via flat-file and SQLite database operations.
 * Implements the generic {@link Repository} interface for type-safe access.
 * Every public method is fully wrapped in try-catch with SLF4J logging at
 * entry, exit, and error points.
 */
public class UserRepository implements Repository<User> {

    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);

    /** Path used for flat-file save/load operations. */
    private final Path filePath;

    /** JDBC URL for the SQLite database. */
    private final String jdbcUrl;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public UserRepository(String filePath, String dbPath) {
        logger.debug("UserRepository constructor: filePath='{}', dbPath='{}'", filePath, dbPath);
        this.filePath = Paths.get(filePath);
        this.jdbcUrl = "jdbc:sqlite:" + dbPath;
        logger.info("UserRepository initialised: file='{}', db='{}'", filePath, dbPath);
    }

    // -----------------------------------------------------------------------
    // Flat-file operations
    // -----------------------------------------------------------------------

    /**
     * Saves a string representation of the user to a text file.
     *
     * @param user the user to save
     * @throws InvalidHabitException if the user is null
     */
    public void saveUserToFile(User user) throws InvalidHabitException {
        logger.debug("saveUserToFile() called for user='{}'", user);
        if (user == null) {
            logger.error("saveUserToFile() received a null user");
            throw new InvalidHabitException("Cannot save a null user to file");
        }
        try {
            Files.writeString(filePath, user.toString(), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
            logger.info("User saved to file '{}': {}", filePath, user);
        } catch (IOException e) {
            logger.error("IO error while saving user to file '{}': {}", filePath, e.getMessage(), e);
            throw new UncheckedIOException("Failed to save user to file: " + filePath, e);
        }
        logger.debug("saveUserToFile() completed for user='{}'", user);
    }

    /**
     * Loads and returns the raw user string from the text file.
     *
     * @return the content of the saved file, or {@code null} if the file does not exist
     */
    public String loadUserFromFile() {
        logger.debug("loadUserFromFile() called, file='{}'", filePath);
        if (!Files.exists(filePath)) {
            logger.warn("File not found: '{}', returning null", filePath);
            return null;
        }
        try {
            String content = Files.readString(filePath);
            logger.info("User loaded from file '{}': {}", filePath, content);
            logger.debug("loadUserFromFile() returning content");
            return content;
        } catch (IOException e) {
            logger.error("IO error while loading user from file '{}': {}", filePath, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Deletes the user file if it exists.
     */
    public void deleteUserFile() {
        logger.debug("deleteUserFile() called, file='{}'", filePath);
        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                logger.info("User file deleted: '{}'", filePath);
            } else {
                logger.warn("deleteUserFile(): file did not exist: '{}'", filePath);
            }
        } catch (IOException e) {
            logger.error("IO error while deleting file '{}': {}", filePath, e.getMessage(), e);
        }
        logger.debug("deleteUserFile() completed");
    }

    // -----------------------------------------------------------------------
    // Database operations
    // -----------------------------------------------------------------------

    /**
     * Creates the 'users' table in SQLite if it does not already exist.
     */
    public void initDatabase() {
        logger.debug("initDatabase() called, url='{}'", jdbcUrl);
        String sql = "CREATE TABLE IF NOT EXISTS users ("
                + "id      INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT    NOT NULL UNIQUE,"
                + "data    TEXT    NOT NULL"
                + ");";
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            logger.info("Database initialised (table 'users' ready), url='{}'", jdbcUrl);

        } catch (SQLException e) {
            logger.error("SQL error while initialising database: {}", e.getMessage(), e);
        }
        logger.debug("initDatabase() completed");
    }

    /**
     * Inserts or replaces a user record in the SQLite database.
     *
     * @param user the user to persist
     * @throws InvalidHabitException if the user is null
     */
    public void saveUserToDb(User user) throws InvalidHabitException {
        logger.debug("saveUserToDb() called for user='{}'", user);
        if (user == null) {
            logger.error("saveUserToDb() received a null user");
            throw new InvalidHabitException("Cannot save a null user to the database");
        }
        String sql = "INSERT OR REPLACE INTO users (username, data) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.toString());
            ps.executeUpdate();
            logger.info("User '{}' saved to database", user.getUsername());

        } catch (SQLException e) {
            logger.error("SQL error while saving user '{}' to database: {}", user.getUsername(), e.getMessage(), e);
        }
        logger.debug("saveUserToDb() completed");
    }

    /**
     * Loads a user's string representation from the database by username.
     *
     * @param username the username to look up
     * @return the stored data string, or {@code null} if not found
     * @throws HabitNotFoundException if no record exists for the given username
     */
    public String loadUserFromDb(String username) throws HabitNotFoundException {
        logger.debug("loadUserFromDb() called, username='{}'", username);
        String sql = "SELECT data FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String data = rs.getString("data");
                    logger.info("User '{}' loaded from database", username);
                    logger.debug("loadUserFromDb() returning data");
                    return data;
                } else {
                    logger.error("User '{}' not found in database", username);
                    throw new HabitNotFoundException("User not found in database: " + username);
                }
            }
        } catch (SQLException e) {
            logger.error("SQL error while loading user '{}' from database: {}", username, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Deletes a user record from the database by username.
     *
     * @param username the username to delete
     * @throws HabitNotFoundException if no record exists for the given username
     */
    public void deleteUserFromDb(String username) throws HabitNotFoundException {
        logger.debug("deleteUserFromDb() called, username='{}'", username);
        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                logger.error("deleteUserFromDb(): no record found for username='{}'", username);
                throw new HabitNotFoundException("Cannot delete – user not found in database: " + username);
            }
            logger.info("User '{}' deleted from database ({} row(s) affected)", username, rows);

        } catch (SQLException e) {
            logger.error("SQL error while deleting user '{}': {}", username, e.getMessage(), e);
        }
        logger.debug("deleteUserFromDb() completed");
    }

    // -----------------------------------------------------------------------
    // Repository<User> interface implementation
    // -----------------------------------------------------------------------

    @Override
    public void save(User entity) throws InvalidHabitException {
        logger.debug("Repository.save() called for User '{}'", entity);
        saveUserToDb(entity);
    }

    @Override
    public Optional<User> findByName(String name) {
        logger.debug("Repository.findByName() called, name='{}'", name);
        try {
            String data = loadUserFromDb(name);
            if (data != null) {
                // Reconstruct a lightweight User object from stored username
                User user = new User(name);
                logger.info("Repository.findByName() found user '{}'", name);
                return Optional.of(user);
            }
        } catch (HabitNotFoundException e) {
            logger.warn("Repository.findByName(): user '{}' not found", name);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        logger.debug("Repository.findAll() called");
        List<User> users = new ArrayList<>();
        String sql = "SELECT username FROM users";
        try (Connection conn = DriverManager.getConnection(jdbcUrl);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new User(rs.getString("username")));
            }
            logger.info("Repository.findAll() returned {} user(s)", users.size());
        } catch (SQLException e) {
            logger.error("SQL error in Repository.findAll(): {}", e.getMessage(), e);
        }
        return users;
    }

    @Override
    public void delete(String name) throws HabitNotFoundException {
        logger.debug("Repository.delete() called, name='{}'", name);
        deleteUserFromDb(name);
    }
}

