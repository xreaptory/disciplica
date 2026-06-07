ALTER TABLE habits
ADD COLUMN metadata_json TEXT NOT NULL DEFAULT '{}';

CREATE VIRTUAL TABLE IF NOT EXISTS habits_fts USING fts5(
    title,
    description,
    content='habits',
    content_rowid='id'
);

INSERT INTO habits_fts(rowid, title, description)
SELECT id, title, description FROM habits;

CREATE TRIGGER IF NOT EXISTS habits_ai AFTER INSERT ON habits BEGIN
    INSERT INTO habits_fts(rowid, title, description)
    VALUES (new.id, new.title, new.description);
END;

CREATE TRIGGER IF NOT EXISTS habits_ad AFTER DELETE ON habits BEGIN
    INSERT INTO habits_fts(habits_fts, rowid, title, description)
    VALUES ('delete', old.id, old.title, old.description);
END;

CREATE TRIGGER IF NOT EXISTS habits_au AFTER UPDATE ON habits BEGIN
    INSERT INTO habits_fts(habits_fts, rowid, title, description)
    VALUES ('delete', old.id, old.title, old.description);
    INSERT INTO habits_fts(rowid, title, description)
    VALUES (new.id, new.title, new.description);
END;
