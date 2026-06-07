package com.disciplica.server.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserRow create(String username, String email) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO users (username, email)
                VALUES (?, ?)
                RETURNING id, username, email, level, xp, health, gold
                """, (rs, rowNum) -> map(rs), username, email.toLowerCase());
    }

    public Optional<UserRow> findById(UUID id) {
        return jdbcTemplate.query("""
                SELECT id, username, email, level, xp, health, gold
                FROM users WHERE id = ?
                """, (rs, rowNum) -> map(rs), id).stream().findFirst();
    }

    public Optional<UserRow> findByEmail(String email) {
        return jdbcTemplate.query("""
                SELECT id, username, email, level, xp, health, gold
                FROM users WHERE lower(email) = lower(?)
                """, (rs, rowNum) -> map(rs), email).stream().findFirst();
    }

    public Optional<UserRow> findByUsernameOrEmail(String value) {
        return jdbcTemplate.query("""
                SELECT id, username, email, level, xp, health, gold
                FROM users
                WHERE lower(username) = lower(?) OR lower(email) = lower(?)
                """, (rs, rowNum) -> map(rs), value, value).stream().findFirst();
    }

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
