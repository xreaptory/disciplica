package com.disciplica.server.task;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.disciplica.shared.task.CreateTaskRequest;
import com.disciplica.shared.task.TaskDto;
import com.disciplica.shared.task.TaskType;
import com.disciplica.shared.task.UpdateTaskRequest;

@Repository
public class TaskRepository {
    private final JdbcTemplate jdbcTemplate;

    public TaskRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<TaskDto> findByUser(UUID userId) {
        return jdbcTemplate.query("""
                SELECT id, type, title, description, points, streak, completed, category, created_at, updated_at
                FROM tasks WHERE user_id = ? ORDER BY created_at DESC
                """, (rs, rowNum) -> map(rs), userId);
    }

    public Optional<TaskDto> findByUserAndId(UUID userId, UUID taskId) {
        return jdbcTemplate.query("""
                SELECT id, type, title, description, points, streak, completed, category, created_at, updated_at
                FROM tasks WHERE user_id = ? AND id = ?
                """, (rs, rowNum) -> map(rs), userId, taskId).stream().findFirst();
    }

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

    public boolean delete(UUID userId, UUID taskId) {
        return jdbcTemplate.update("DELETE FROM tasks WHERE user_id = ? AND id = ?", userId, taskId) > 0;
    }

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
