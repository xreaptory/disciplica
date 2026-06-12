-- Phase 1 (Kern-Score-Loop): Zeitpunkt des letzten Tageswechsels (Cron) je
-- Benutzer. Wird genutzt, um Dailies einmal pro Tag zurückzusetzen und für
-- verpasste Dailies Schaden auf die Lebenspunkte anzuwenden.
ALTER TABLE users ADD COLUMN last_cron TIMESTAMPTZ NOT NULL DEFAULT now();
