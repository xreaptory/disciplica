ALTER TABLE habits
ADD COLUMN completed INTEGER NOT NULL DEFAULT 0;

ALTER TABLE habits
ADD COLUMN created_at TEXT NOT NULL DEFAULT (datetime('now'));

ALTER TABLE habits
ADD COLUMN updated_at TEXT NOT NULL DEFAULT (datetime('now'));
