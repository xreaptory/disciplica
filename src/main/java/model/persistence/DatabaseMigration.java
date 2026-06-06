package model.persistence;

import org.flywaydb.core.Flyway;

public final class DatabaseMigration {
    private static final String DEFAULT_DB_URL = "jdbc:sqlite:data/habittracker.db";

    private DatabaseMigration() {
    }

    public static void migrateOnStartup() {
        migrate(DEFAULT_DB_URL);
    }

    public static void migrate(String jdbcUrl) {
        Flyway flyway = Flyway.configure()
                .dataSource(jdbcUrl, null, null)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
    }
}
