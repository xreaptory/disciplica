# Disciplica User Manual (v1.0)

## 1. Getting Started

1. Launch the app.
2. Use the left navigation:
   - `Dashboard`
   - `Habits`
   - `Stats`

## 2. Habits Board

In `Habits`:

1. Enter `Name`, `Description`, and `Points`.
2. Select type (`Daily Habit`, `Weekly Habit`, `OneTimeTask`).
3. Click `Add`.
4. Select an existing item and use:
   - `Change`
   - `Remove`
   - `Save` (persist file state)

Double-click a habit row to open detail view, then click `Complete`.

## 3. Dashboard Analytics

In `Dashboard`:

- Weekly completion trend
- Category strength
- Current streak ranking
- XP history

Use range selector:
- Last 7 Days
- Last 30 Days
- Last Year

Click `Export Charts` to save a PNG image.

## 4. Player Stats & Shop

In `Stats`:

- View Level, XP, Gold, Health
- XP and HP progress bars
- Avatar equipment panel
- Equipment shop (spends Gold)

## 5. Backup & Restore

Backups are encrypted JSON files.

- Set `DISCIPLICA_BACKUP_KEY` for secure encryption key handling.
- CSV export is available for spreadsheet analysis.

## 6. Keyboard Shortcuts

- `Ctrl + D` Dashboard
- `Ctrl + H` Habits
- `Ctrl + T` Stats
- `Ctrl + S` Save
- `Ctrl + A` Add
- `Ctrl + R` Remove
- `Ctrl + E` Change

## 7. Notifications

Reminder system runs daily and checks incomplete habits.
System tray menu supports:
- Check now
- Snooze 10 minutes
- Snooze 30 minutes

## 8. Troubleshooting

- If data is not loading, verify write permissions to `data/`.
- If tray notifications are missing, check OS tray support.
- If charts fail export, ensure target path is writable.
