PRAGMA foreign_keys = ON;

-- Drop order for repeatable local testing.
DROP TABLE IF EXISTS completions;
DROP TABLE IF EXISTS habits;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE,
    level INTEGER NOT NULL DEFAULT 1 CHECK (level >= 1),
    xp INTEGER NOT NULL DEFAULT 0 CHECK (xp >= 0),
    health INTEGER NOT NULL DEFAULT 50 CHECK (health BETWEEN 0 AND 50),
    gold INTEGER NOT NULL DEFAULT 0 CHECK (gold >= 0)
);

CREATE TABLE habits (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    difficulty TEXT NOT NULL CHECK (difficulty IN ('easy', 'medium', 'hard')),
    frequency TEXT NOT NULL CHECK (frequency IN ('daily', 'weekly', 'monthly')),
    CONSTRAINT fk_habits_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT uq_habit_title_per_user UNIQUE (user_id, title)
);

CREATE TABLE completions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    habit_id INTEGER NOT NULL,
    completed_at TEXT NOT NULL DEFAULT (datetime('now')),
    quality INTEGER NOT NULL CHECK (quality BETWEEN -1 AND 1),
    CONSTRAINT fk_completions_habit
        FOREIGN KEY (habit_id) REFERENCES habits(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

CREATE INDEX idx_habits_user_id ON habits(user_id);
CREATE INDEX idx_completions_habit_id ON completions(habit_id);
CREATE INDEX idx_completions_completed_at ON completions(completed_at);

-- ------------------------------------------------------------
-- Practice commands (can be run manually for CRUD rehearsal)
-- ------------------------------------------------------------

-- INSERT
INSERT INTO users (username, email, level, xp, health, gold)
VALUES ('simon', 'simon@example.com', 3, 220, 45, 120);

INSERT INTO habits (user_id, title, description, difficulty, frequency)
VALUES (
    (SELECT id FROM users WHERE username = 'simon'),
    'Morning Run',
    'Run 5km before work',
    'medium',
    'daily'
);

INSERT INTO completions (habit_id, completed_at, quality)
VALUES (
    (SELECT id FROM habits WHERE title = 'Morning Run' AND user_id = (SELECT id FROM users WHERE username = 'simon')),
    datetime('now'),
    1
);

-- SELECT
SELECT u.username, h.title, c.completed_at, c.quality
FROM completions c
JOIN habits h ON h.id = c.habit_id
JOIN users u ON u.id = h.user_id
ORDER BY c.completed_at DESC;

-- UPDATE
UPDATE users
SET xp = xp + 20, gold = gold + 5
WHERE username = 'simon';

UPDATE habits
SET difficulty = 'hard'
WHERE title = 'Morning Run'
  AND user_id = (SELECT id FROM users WHERE username = 'simon');

-- DELETE
DELETE FROM completions
WHERE habit_id = (
    SELECT id FROM habits
    WHERE title = 'Morning Run'
      AND user_id = (SELECT id FROM users WHERE username = 'simon')
)
AND quality = -1;

DELETE FROM habits
WHERE title = 'Morning Run'
  AND user_id = (SELECT id FROM users WHERE username = 'simon');
