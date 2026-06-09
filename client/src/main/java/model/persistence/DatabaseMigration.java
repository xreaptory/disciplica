package model.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.flywaydb.core.Flyway;

/**
 * Führt die Datenbank-Migrationen (Flyway) aus, damit das Datenbankschema
 * beim Start auf dem aktuellen Stand ist.
 */
public final class DatabaseMigration {
    private static final String DEFAULT_DB_URL = "jdbc:sqlite:data/habittracker.db";

    private DatabaseMigration() {
    }

    /**
     * Führt die Migrationen auf der Standard-SQLite-Datenbank aus. Wird beim
     * Anwendungsstart aufgerufen.
     */
    public static void migrateOnStartup() {
        migrate(DEFAULT_DB_URL);
    }

    /**
     * Führt die Migrationen auf der angegebenen Datenbank aus.
     *
     * @param jdbcUrl die JDBC-Adresse der Datenbank
     */
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

    /**
     * Stellt bei einer SQLite-Datenbank sicher, dass das Zielverzeichnis für
     * die Datenbankdatei existiert.
     *
     * @param jdbcUrl die JDBC-Adresse der Datenbank
     * @throws DatabaseException wenn das Verzeichnis nicht angelegt werden kann
     */
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
