package com.disciplica.server.task;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.disciplica.shared.task.CreateTaskRequest;
import com.disciplica.shared.task.DailyActivityDto;
import com.disciplica.shared.task.TaskDto;
import com.disciplica.shared.task.TaskType;
import com.disciplica.shared.task.UpdateTaskRequest;

/**
 * Datenbankzugriff auf die Aufgabentabelle: Suchen, Anlegen, Ändern, Löschen
 * und Abschließen von Aufgaben.
 */
@Repository
public class TaskRepository {
    private final JdbcTemplate jdbcTemplate;

    /**
     * Erzeugt das Repository mit dem Datenbankzugriff.
     *
     * @param jdbcTemplate der Datenbankzugriff
     */
    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Liefert alle Aufgaben eines Benutzers, neueste zuerst.
     *
     * @param userId die Kennung des Benutzers
     * @return die Liste der Aufgaben
     */
    public List<TaskDto> findByUser(UUID userId) {
        return jdbcTemplate.query("""
                SELECT id, type, title, description, points, streak, completed, category, created_at, updated_at
                FROM tasks WHERE user_id = ? ORDER BY created_at DESC
                """, (rs, rowNum) -> map(rs), userId);
    }

    /**
     * Sucht eine bestimmte Aufgabe eines Benutzers.
     *
     * @param userId die Kennung des Benutzers
     * @param taskId die Kennung der Aufgabe
     * @return die Aufgabe oder ein leeres {@link Optional}, falls nicht
     *         vorhanden
     */
    public Optional<TaskDto> findByUserAndId(UUID userId, UUID taskId) {
        return jdbcTemplate.query("""
                SELECT id, type, title, description, points, streak, completed, category, created_at, updated_at
                FROM tasks WHERE user_id = ? AND id = ?
                """, (rs, rowNum) -> map(rs), userId, taskId).stream().findFirst();
    }

    /**
     * Legt eine neue Aufgabe an und setzt sinnvolle Standardwerte für leere
     * Felder.
     *
     * @param userId  die Kennung des Benutzers
     * @param request die Daten der neuen Aufgabe
     * @return die angelegte Aufgabe
     */
    public TaskDto create(UUID userId, CreateTaskRequest request) {
        return jdbcTemplate.queryForObject("""
                INSERT INTO tasks (user_id, type, title, description, points, category)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id, type, title, description, points, streak, completed, category, created_at, updated_at
                """, (rs, rowNum) -> map(rs),
                userId,
                request.type().name(),
                request.title(),
                request.description() == null ? "" : request.description(),
                Math.max(0, request.points()),
                request.category() == null || request.category().isBlank() ? "General" : request.category());
    }

    /**
     * Aktualisiert die übergebenen Felder einer Aufgabe. Nicht gesetzte Werte
     * ({@code null}) bleiben unverändert.
     *
     * @param userId  die Kennung des Benutzers
     * @param taskId  die Kennung der Aufgabe
     * @param request die zu ändernden Felder
     * @return die aktualisierte Aufgabe oder ein leeres {@link Optional},
     *         falls keine passende Aufgabe existiert
     */
    public Optional<TaskDto> update(UUID userId, UUID taskId, UpdateTaskRequest request) {
        return jdbcTemplate.query("""
                UPDATE tasks SET
                    title = COALESCE(?, title),
                    description = COALESCE(?, description),
                    points = COALESCE(?, points),
                    completed = COALESCE(?, completed),
                    category = COALESCE(?, category),
                    updated_at = now()
                WHERE user_id = ? AND id = ?
                RETURNING id, type, title, description, points, streak, completed, category, created_at, updated_at
                """, (rs, rowNum) -> map(rs),
                request.title(),
                request.description(),
                request.points(),
                request.completed(),
                request.category(),
                userId,
                taskId).stream().findFirst();
    }

    /**
     * Löscht eine Aufgabe eines Benutzers.
     *
     * @param userId die Kennung des Benutzers
     * @param taskId die Kennung der Aufgabe
     * @return {@code true}, wenn eine Aufgabe gelöscht wurde
     */
    public boolean delete(UUID userId, UUID taskId) {
        return jdbcTemplate.update("DELETE FROM tasks WHERE user_id = ? AND id = ?", userId, taskId) > 0;
    }

    /**
     * Markiert eine Aufgabe als erledigt, erhöht bei Gewohnheiten und Dailies
     * die Serie und schreibt dem Benutzer in einem Schritt Erfahrungspunkte
     * und Gold gut.
     *
     * @param userId die Kennung des Benutzers
     * @param taskId die Kennung der Aufgabe
     * @return die abgeschlossene Aufgabe oder ein leeres {@link Optional},
     *         falls keine passende Aufgabe existiert
     */
    public Optional<TaskDto> complete(UUID userId, UUID taskId) {
        return jdbcTemplate.query("""
                WITH updated AS (
                    UPDATE tasks
                    SET completed = true,
                        streak = CASE WHEN type IN ('HABIT', 'DAILY') THEN streak + 1 ELSE streak END,
                        updated_at = now()
                    WHERE user_id = ? AND id = ?
                    RETURNING id, type, title, description, points, streak, completed, category, created_at, updated_at
                ), rewards AS (
                    UPDATE users
                    SET xp = xp + (SELECT points FROM updated),
                        gold = gold + GREATEST(1, (SELECT points FROM updated) / 10),
                        updated_at = now()
                    WHERE id = ?
                ), completion AS (
                    INSERT INTO task_completions (task_id, user_id, xp_earned, gold_earned)
                    SELECT id, ?, points, GREATEST(1, points / 10) FROM updated
                )
                SELECT * FROM updated
                """, (rs, rowNum) -> map(rs), userId, taskId, userId, userId).stream().findFirst();
    }

    /**
     * Liefert die tagesweise zusammengefasste Aktivität eines Benutzers über die
     * letzten {@code days} Tage (nur Tage mit Aktivität, aufsteigend).
     *
     * @param userId die Kennung des Benutzers
     * @param days   die Anzahl der betrachteten Tage
     * @return die Liste der Tagesaktivitäten
     */
    public List<DailyActivityDto> dailyActivity(UUID userId, int days) {
        return jdbcTemplate.query("""
                SELECT to_char((completed_at AT TIME ZONE 'UTC')::date, 'YYYY-MM-DD') AS day,
                       COUNT(*)::int AS completions,
                       COALESCE(SUM(xp_earned), 0)::int AS xp
                FROM task_completions
                WHERE user_id = ? AND completed_at >= now() - ? * INTERVAL '1 day'
                GROUP BY (completed_at AT TIME ZONE 'UTC')::date
                ORDER BY (completed_at AT TIME ZONE 'UTC')::date
                """, (rs, rowNum) -> new DailyActivityDto(
                rs.getString("day"),
                rs.getInt("completions"),
                rs.getInt("xp")
        ), userId, days);
    }

    /**
     * Bewertet eine Gewohnheit negativ („−“): verringert die Serie und fügt dem
     * Benutzer Schaden auf die Lebenspunkte zu (mindestens 1, abhängig vom
     * Punktewert). Es werden keine Erfahrungspunkte oder Gold vergeben.
     *
     * @param userId die Kennung des Benutzers
     * @param taskId die Kennung der Aufgabe
     * @return die aktualisierte Aufgabe oder ein leeres {@link Optional}, falls
     *         keine passende Aufgabe existiert
     */
    public Optional<TaskDto> scoreDown(UUID userId, UUID taskId) {
        return jdbcTemplate.query("""
                WITH updated AS (
                    UPDATE tasks
                    SET streak = GREATEST(0, streak - 1), updated_at = now()
                    WHERE user_id = ? AND id = ?
                    RETURNING id, type, title, description, points, streak, completed, category, created_at, updated_at
                ), dmg AS (
                    UPDATE users
                    SET health = GREATEST(0, health - GREATEST(1, (SELECT points FROM updated))),
                        updated_at = now()
                    WHERE id = ? AND EXISTS (SELECT 1 FROM updated)
                )
                SELECT * FROM updated
                """, (rs, rowNum) -> map(rs), userId, taskId, userId).stream().findFirst();
    }

    /**
     * Wandelt die aktuelle Zeile eines Datenbankergebnisses in ein
     * {@link TaskDto} um.
     *
     * @param rs das Datenbankergebnis, positioniert auf der zu lesenden Zeile
     * @return die gelesene Aufgabe
     * @throws SQLException bei einem Fehler beim Auslesen der Spalten
     */
    private TaskDto map(ResultSet rs) throws SQLException {
        return new TaskDto(
                rs.getObject("id", UUID.class),
                TaskType.valueOf(rs.getString("type")),
                rs.getString("title"),
                rs.getString("description"),
                rs.getInt("points"),
                rs.getInt("streak"),
                rs.getBoolean("completed"),
                rs.getString("category"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getTimestamp("updated_at").toInstant()
        );
    }
}
