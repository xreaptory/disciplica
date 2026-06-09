package com.disciplica.server.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * Datenbankzugriff auf die Benutzertabelle: Anlegen und Suchen von Benutzern.
 */
@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    /**
     * Erzeugt das Repository mit dem Datenbankzugriff.
     *
     * @param jdbcTemplate der Datenbankzugriff
     */
    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Legt einen neuen Benutzer an (E-Mail wird kleingeschrieben gespeichert).
     *
     * @param username der Benutzername
     * @param email    die E-Mail-Adresse
     * @return die angelegte Benutzerzeile
     */
    public UserRow create(String username, String email) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO users (username, email)
                VALUES (?, ?)
                RETURNING id, username, email, level, xp, health, gold
                """, (rs, rowNum) -> map(rs), username, email.toLowerCase());
    }

    /**
     * Sucht einen Benutzer anhand seiner Kennung.
     *
     * @param id die Kennung des Benutzers
     * @return der Benutzer oder ein leeres {@link Optional}, falls nicht
     *         vorhanden
     */
    public Optional<UserRow> findById(UUID id) {
        return jdbcTemplate.query("""
                SELECT id, username, email, level, xp, health, gold
                FROM users WHERE id = ?
                """, (rs, rowNum) -> map(rs), id).stream().findFirst();
    }

    /**
     * Sucht einen Benutzer anhand seiner E-Mail-Adresse (ohne
     * Groß-/Kleinschreibung).
     *
     * @param email die gesuchte E-Mail-Adresse
     * @return der Benutzer oder ein leeres {@link Optional}, falls nicht
     *         vorhanden
     */
    public Optional<UserRow> findByEmail(String email) {
        return jdbcTemplate.query("""
                SELECT id, username, email, level, xp, health, gold
                FROM users WHERE lower(email) = lower(?)
                """, (rs, rowNum) -> map(rs), email).stream().findFirst();
    }

    /**
     * Sucht einen Benutzer anhand seines Benutzernamens oder seiner
     * E-Mail-Adresse (ohne Groß-/Kleinschreibung).
     *
     * @param value Benutzername oder E-Mail-Adresse
     * @return der Benutzer oder ein leeres {@link Optional}, falls nicht
     *         vorhanden
     */
    public Optional<UserRow> findByUsernameOrEmail(String value) {
        return jdbcTemplate.query("""
                SELECT id, username, email, level, xp, health, gold
                FROM users
                WHERE lower(username) = lower(?) OR lower(email) = lower(?)
                """, (rs, rowNum) -> map(rs), value, value).stream().findFirst();
    }

    /**
     * Wandelt die aktuelle Zeile eines Datenbankergebnisses in eine
     * {@link UserRow} um.
     *
     * @param resultSet das Datenbankergebnis, positioniert auf der zu lesenden
     *                  Zeile
     * @return die gelesene Benutzerzeile
     * @throws SQLException bei einem Fehler beim Auslesen der Spalten
     */
    private UserRow map(ResultSet resultSet) throws SQLException {
        return new UserRow(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("username"),
                resultSet.getString("email"),
                resultSet.getInt("level"),
                resultSet.getInt("xp"),
                resultSet.getInt("health"),
                resultSet.getInt("gold")
        );
    }
}
