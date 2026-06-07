package model.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.flywaydb.core.Flyway;

public final class DatabaseMigration {
    private static final String DEFAULT_DB_URL = "jdbc:sqlite:data/habittracker.db";

    private DatabaseMigration() {
    }

    public static void migrateOnStartup() {
        migrate(DEFAULT_DB_URL);
    }

    public static void migrate(String jdbcUrl) {
        ensureSqliteDirectoryExists(jdbcUrl);
        Flyway flyway = Flyway.configure()
                .dataSource(jdbcUrl, null, null)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .mixed(true)
                .load();
        flyway.migrate();
    }

    private static void ensureSqliteDirectoryExists(String jdbcUrl) {
        if (!jdbcUrl.startsWith("jdbc:sqlite:")) {
            return;
        }
        String dbPath = jdbcUrl.substring("jdbc:sqlite:".length());
        Path parent = Path.of(dbPath).toAbsolutePath().getParent();
        if (parent == null) {
            return;
        }
        try {
            Files.createDirectories(parent);
        } catch (IOException exception) {
            throw new DatabaseException("Unable to create database directory: " + parent, exception);
        }
    }
}
