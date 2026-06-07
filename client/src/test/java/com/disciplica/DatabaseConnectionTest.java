package com.disciplica;

import model.persistence.DatabaseConnection;
import model.persistence.DatabaseException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseConnectionTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Connect, execute query, validate, and close pooled connections")
    void connectExecuteValidateAndClose() throws Exception {
        Path dbFile = tempDir.resolve("habittracker-test.db");
        String jdbcUrl = "jdbc:sqlite:" + dbFile;

        DatabaseConnection databaseConnection = new DatabaseConnection(jdbcUrl);
        assertTrue(databaseConnection.validateConnection());

        try (Connection connection = databaseConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS test_table (id INTEGER PRIMARY KEY, name TEXT NOT NULL)");
            statement.execute("INSERT INTO test_table (name) VALUES ('alpha')");
        }

        try (Connection connection = databaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) AS total FROM test_table")) {
            assertTrue(resultSet.next());
            assertEquals(1, resultSet.getInt("total"));
        }

        databaseConnection.close();
        assertThrows(DatabaseException.class, databaseConnection::getConnection);
    }
}
