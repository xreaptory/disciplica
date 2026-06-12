-- Der Abschluss-Verlauf (task_completions) soll das Löschen einer Aufgabe
-- überleben, damit das Dashboard die Historie (Erfüllungsquote, XP-Verlauf)
-- behält. Bisher löschte ON DELETE CASCADE den Verlauf mit der Aufgabe.
-- Stattdessen wird die Aufgaben-Referenz beim Löschen auf NULL gesetzt; der
-- Verlauf bleibt (er wird ohnehin nur über user_id und Datum ausgewertet).
ALTER TABLE task_completions DROP CONSTRAINT task_completions_task_id_fkey;
ALTER TABLE task_completions ALTER COLUMN task_id DROP NOT NULL;
ALTER TABLE task_completions ADD CONSTRAINT task_completions_task_id_fkey
    FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE SET NULL;
