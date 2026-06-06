package com.disciplica;

import model.persistence.DatabaseMigration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FlywayMigrationTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Deleting DB and running startup migration recreates full schema")
    void migrationRecreatesSchemaFromScratch() throws Exception {
        Path dbPath = tempDir.resolve("habittracker.db");
        if (Files.exists(dbPath)) {
            Files.delete(dbPath);
        }

        DatabaseMigration.migrate("jdbc:sqlite:" + dbPath);
        assertTrue(Files.exists(dbPath));

        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
             Statement statement = connection.createStatement()) {

            assertTrue(tableExists(statement, "users"));
            assertTrue(tableExists(statement, "habits"));
            assertTrue(tableExists(statement, "completions"));
            assertTrue(columnExists(statement, "users", "preferences_json"));
            assertFalse(columnExists(statement, "users", "non_existing_column"));
        }
    }

    private boolean tableExists(Statement statement, String tableName) throws Exception {
        try (ResultSet rs = statement.executeQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'")) {
            return rs.next();
        }
    }

    private boolean columnExists(Statement statement, String tableName, String columnName) throws Exception {
        try (ResultSet rs = statement.executeQuery("PRAGMA table_info(" + tableName + ")")) {
            while (rs.next()) {
                if (columnName.equals(rs.getString("name"))) {
                    return true;
                }
            }
            return false;
        }
    }
}
