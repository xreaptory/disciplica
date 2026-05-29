PRAGMA foreign_keys = ON;

-- ============================================================
-- Indexes on frequently queried columns
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_habits_user_id ON habits(user_id);
CREATE INDEX IF NOT EXISTS idx_completions_habit_id ON completions(habit_id);
CREATE INDEX IF NOT EXISTS idx_completions_completed_at ON completions(completed_at);

-- ============================================================
-- Q1) All habits for one user + completion count
-- ============================================================
SELECT
    h.id,
    h.title,
    h.description,
    h.difficulty,
    h.frequency,
    COUNT(c.id) AS completion_count
FROM habits h
LEFT JOIN completions c ON c.habit_id = h.id
WHERE h.user_id = ?1
GROUP BY h.id, h.title, h.description, h.difficulty, h.frequency
ORDER BY h.title;

-- ============================================================
-- Q2) All habits for one username + completion count
-- ============================================================
SELECT
    h.id,
    h.title,
    COUNT(c.id) AS completion_count
FROM users u
JOIN habits h ON h.user_id = u.id
LEFT JOIN completions c ON c.habit_id = h.id
WHERE u.username = ?1
GROUP BY h.id, h.title
ORDER BY completion_count DESC, h.title;

-- ============================================================
-- Q3) Daily completion totals for the last 7 days (per user)
-- ============================================================
SELECT
    DATE(c.completed_at) AS day,
    COUNT(*) AS completions
FROM completions c
JOIN habits h ON h.id = c.habit_id
WHERE h.user_id = ?1
  AND DATE(c.completed_at) >= DATE('now', '-6 days')
GROUP BY DATE(c.completed_at)
ORDER BY day;

-- ============================================================
-- Q4) Weekly completion stats (current week, Monday start)
-- ============================================================
SELECT
    h.user_id,
    COUNT(c.id) AS weekly_completions
FROM completions c
JOIN habits h ON h.id = c.habit_id
WHERE DATE(c.completed_at) >= DATE('now', 'weekday 1', '-7 days')
  AND DATE(c.completed_at) < DATE('now', 'weekday 1')
  AND h.user_id = ?1
GROUP BY h.user_id;

-- ============================================================
-- Q5) Habit breakdown for this week (per habit)
-- ============================================================
SELECT
    h.id,
    h.title,
    COUNT(c.id) AS weekly_count
FROM habits h
LEFT JOIN completions c
    ON c.habit_id = h.id
   AND DATE(c.completed_at) >= DATE('now', 'weekday 1', '-7 days')
   AND DATE(c.completed_at) < DATE('now', 'weekday 1')
WHERE h.user_id = ?1
GROUP BY h.id, h.title
ORDER BY weekly_count DESC, h.title;

-- ============================================================
-- Q6) Current streak in days per habit (consecutive completion days)
--      Uses gaps-and-islands on distinct completion dates.
-- ============================================================
WITH distinct_days AS (
    SELECT DISTINCT
        c.habit_id,
        DATE(c.completed_at) AS d
    FROM completions c
),
ordered AS (
    SELECT
        habit_id,
        d,
        ROW_NUMBER() OVER (PARTITION BY habit_id ORDER BY d DESC) AS rn
    FROM distinct_days
),
streak_rows AS (
    SELECT
        o.habit_id,
        o.d,
        o.rn,
        JULIANDAY('now') - JULIANDAY(o.d) AS day_diff
    FROM ordered o
),
current_chain AS (
    SELECT
        habit_id,
        COUNT(*) AS streak_days
    FROM streak_rows
    WHERE CAST(day_diff AS INTEGER) = rn - 1
    GROUP BY habit_id
)
SELECT
    h.id,
    h.title,
    COALESCE(cc.streak_days, 0) AS current_streak_days
FROM habits h
LEFT JOIN current_chain cc ON cc.habit_id = h.id
WHERE h.user_id = ?1
ORDER BY current_streak_days DESC, h.title;

-- ============================================================
-- Q7) Longest historical streak per habit
-- ============================================================
WITH distinct_days AS (
    SELECT DISTINCT habit_id, DATE(completed_at) AS d
    FROM completions
),
grouped AS (
    SELECT
        habit_id,
        d,
        JULIANDAY(d) - ROW_NUMBER() OVER (PARTITION BY habit_id ORDER BY d) AS grp
    FROM distinct_days
),
chains AS (
    SELECT
        habit_id,
        COUNT(*) AS streak_len
    FROM grouped
    GROUP BY habit_id, grp
)
SELECT
    h.id,
    h.title,
    COALESCE(MAX(ch.streak_len), 0) AS longest_streak_days
FROM habits h
LEFT JOIN chains ch ON ch.habit_id = h.id
WHERE h.user_id = ?1
GROUP BY h.id, h.title
ORDER BY longest_streak_days DESC, h.title;

-- ============================================================
-- Q8) Habits not completed in the last N days (example N=3)
-- ============================================================
SELECT
    h.id,
    h.title
FROM habits h
LEFT JOIN completions c
    ON c.habit_id = h.id
   AND DATE(c.completed_at) >= DATE('now', '-3 days')
WHERE h.user_id = ?1
GROUP BY h.id, h.title
HAVING COUNT(c.id) = 0
ORDER BY h.title;

-- ============================================================
-- Q9) User summary stats (habit count, total completions, avg quality)
-- ============================================================
SELECT
    u.id,
    u.username,
    COUNT(DISTINCT h.id) AS habit_count,
    COUNT(c.id) AS total_completions,
    ROUND(AVG(c.quality), 2) AS avg_quality
FROM users u
LEFT JOIN habits h ON h.user_id = u.id
LEFT JOIN completions c ON c.habit_id = h.id
WHERE u.id = ?1
GROUP BY u.id, u.username;

-- ============================================================
-- Q10) UPDATE with JOIN pattern: add XP based on this week's completions
--      (+10 XP per completion this week)
-- ============================================================
UPDATE users
SET xp = xp + COALESCE((
    SELECT COUNT(c.id) * 10
    FROM completions c
    JOIN habits h ON h.id = c.habit_id
    WHERE h.user_id = users.id
      AND DATE(c.completed_at) >= DATE('now', 'weekday 1', '-7 days')
      AND DATE(c.completed_at) < DATE('now', 'weekday 1')
), 0)
WHERE id = ?1;

-- ============================================================
-- Q11) UPDATE with JOIN pattern: sync difficulty to hard for low quality
--      (if avg quality < 0)
-- ============================================================
UPDATE habits
SET difficulty = 'hard'
WHERE id IN (
    SELECT h.id
    FROM habits h
    JOIN completions c ON c.habit_id = h.id
    WHERE h.user_id = ?1
    GROUP BY h.id
    HAVING AVG(c.quality) < 0
);

-- ============================================================
-- Q12) DELETE old completions (data retention example)
-- ============================================================
DELETE FROM completions
WHERE DATE(completed_at) < DATE('now', '-365 days');
