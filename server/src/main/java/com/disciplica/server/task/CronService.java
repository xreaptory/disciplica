package com.disciplica.server.task;

import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Führt den täglichen Tageswechsel (Cron) für einen Benutzer aus.
 * <p>
 * Wie bei Habitica läuft der Cron nicht über einen festen Zeitplan (eine
 * schlafende Render-Instanz würde ihn verpassen), sondern bei der ersten
 * Aktivität an einem neuen Tag: Jede verpasste wiederkehrende Aufgabe (Daily
 * wie Habit, nicht erledigt) fügt automatisch Schaden auf die Lebenspunkte zu
 * und setzt die Serie zurück; anschließend werden alle wiederkehrenden Aufgaben
 * wieder auf „offen“ gesetzt.
 */
@Service
public class CronService {
    private final JdbcTemplate jdbcTemplate;

    /**
     * Erzeugt den Dienst mit dem Datenbankzugriff.
     *
     * @param jdbcTemplate der Datenbankzugriff
     */
    public CronService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Führt den Tageswechsel aus, falls seit dem letzten Cron (in UTC) ein
     * neuer Kalendertag begonnen hat. Andernfalls passiert nichts.
     *
     * @param userId die Kennung des Benutzers
     */
    @Transactional
    public void runIfNeeded(UUID userId) {
        Integer due = jdbcTemplate.queryForObject("""
                SELECT CASE WHEN (last_cron AT TIME ZONE 'UTC')::date < (now() AT TIME ZONE 'UTC')::date
                            THEN 1 ELSE 0 END
                FROM users WHERE id = ?
                """, Integer.class, userId);
        if (due == null || due == 0) {
            return;
        }
        // 1) Schaden für verpasste (nicht erledigte) wiederkehrende Aufgaben –
        //    vor dem Zurücksetzen. Eigenes Design: jede wiederkehrende Aufgabe
        //    (Daily wie Habit) zieht bei Versäumnis automatisch HP ab.
        jdbcTemplate.update("""
                UPDATE users u
                SET health = GREATEST(0, health - COALESCE((
                        SELECT SUM(GREATEST(1, t.points)) FROM tasks t
                        WHERE t.user_id = u.id AND t.type IN ('DAILY', 'HABIT') AND t.completed = false
                    ), 0)),
                    updated_at = now()
                WHERE u.id = ?
                """, userId);
        // 2) Wiederkehrende Aufgaben zurücksetzen: verpasste verlieren ihre
        //    Serie, alle werden wieder offen.
        jdbcTemplate.update("""
                UPDATE tasks
                SET streak = CASE WHEN completed THEN streak ELSE 0 END,
                    completed = false,
                    updated_at = now()
                WHERE user_id = ? AND type IN ('DAILY', 'HABIT')
                """, userId);
        // 3) Tageswechsel vermerken.
        jdbcTemplate.update("UPDATE users SET last_cron = now() WHERE id = ?", userId);
    }
}
