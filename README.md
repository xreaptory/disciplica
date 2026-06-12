# Disciplica – User Documentation (v1.0)

Disciplica is a habit and task tracker with RPG mechanics.
Completing tasks gives XP and gold and lets you level up; missing tasks costs health points.
Optionally, data can be shared through a server (groups, chat).

---

## 1. System Requirements

- **Operating system:** Windows, macOS, or Linux
- **Java:** Version 17 or newer (JDK or JRE)
- **Internet:** Only required for online features (server login, groups, Google login). Offline mode works without internet.

Check if Java is installed:

```bash
java -version
```

If version 17 or higher is shown, you are ready.

---

## 2. Installation and Start

The app is distributed as an **executable JAR**. No installer is required.

1. Put `disciplica-client-1.0-SNAPSHOT-consumer.jar` into any folder.
2. Start the app:

   ```bash
   java -jar disciplica-client-1.0-SNAPSHOT-consumer.jar
   ```

### Windows-specific steps

1. Install **Java 17+** (e.g. Temurin/OpenJDK).
2. Open **PowerShell** in the folder containing the JAR.
3. Run:

   ```powershell
   java -jar .\disciplica-client-1.0-SNAPSHOT-consumer.jar
   ```

You can also try starting the JAR with double-click if Java is correctly associated.

On first start, the app creates a local database in the `data/` subfolder.
That folder must be writable.

---

## 3. Login

After startup, the login window offers three options:

| Option | Description |
|--------|-------------|
| **Login / Register** | Login with email and password. Use the link at the bottom to switch between login and registration. Registration also requires a username. |
| **Continue with Google** | Login with a Google account. Your default browser opens; after successful login, you return to the app. |
| **Continue Offline** | Start without server access. Data is stored only on this computer. Group and Google features are unavailable. |

**Server note:** The hosted server can need up to ~60 seconds to wake up on first request.

**Registration input rules:**

- Username: 3 to 32 characters
- Email: valid format (e.g. `name@example.com`)
- Password: 10 to 128 characters

---

## 4. Main Window Layout

The left navigation bar has four areas:

- **Dashboard** – charts and analytics
- **Habits** – manage habits and tasks
- **Stats** – player values, avatar, and shop
- **Party** – group, members, and chat

The active area is highlighted. The main panel on the right shows its content.

---

## 5. Manage Habits ("Habits" area)

### 5.1 Add a task

1. Enter **Name**, **Description**, and **Points**.
2. Select task type:
   - **Daily Habit** – daily habit (tracks streak)
   - **Weekly Habit** – weekly habit (higher streak bonus)
   - **OneTimeTask** – one-time task (to-do)
3. Click **Add**.

### 5.2 Edit or remove a task

1. Select an entry in the list (fields are auto-filled).
2. Change values and click **Change**, or click **Remove** to delete.

### 5.3 Complete a task

1. Double-click a task in the list to open details.
2. Click **Complete**.

Completing gives XP and gold; daily/weekly habits also increase streak.
One-time tasks are removed after completion.

### 5.4 Save

Use **Save** to persist data. When logged in, syncing to server is automatic.

---

## 6. Dashboard ("Dashboard" area)

Dashboard visualizes progress:

- Completion rate in selected period
- Strength by category
- Current streak ranking
- XP progress over time

Choose a period (last 7 days, last 30 days, last year, etc.).
Use **Export Charts** to save PNG files.

---

## 7. Player Stats, Avatar, and Shop ("Stats" area)

In **Stats** you can view and configure:

- **Level, XP, gold, and health** with progress bars
- **Avatar:** body size, skin/hair/clothes color, and hairstyle
- **Shop:** buy equipment (armor, headgear, weapon, etc.) with gold

A short animation is shown when you reach a new level.

---

## 8. Groups and Chat ("Party" area)

> Available only when logged in (not in offline mode).

In **Party** you can:

- create a **group** (you become leader),
- invite others by **username or email** (leader only),
- **accept or decline** invitations,
- chat with members in real time.

---

## 9. Reminders (System Tray)

Disciplica shows a tray icon and reminds once per day about open habits.
Tray context menu options:

- **Check now**
- **Snooze 10 minutes** / **Snooze 30 minutes**
- **Disable reminders**

> Requires an operating system with system tray support.

---

## 10. Backup, Restore, and Export

- **Encrypted backup:** user data can be exported/imported as encrypted JSON.
  Depending on settings, automatic backups are saved daily/weekly in `data/cloud-sync/`.
- **Encryption key:** set your own encryption password with `DISCIPLICA_BACKUP_KEY`.
  If unset, a development default key is used (not recommended for production).
- **CSV export:** habits can be exported as CSV.

---

## 11. Keyboard Shortcuts

| Shortcut | Action |
|----------|--------|
| `Ctrl + D` | Open Dashboard |
| `Ctrl + H` | Open Habits |
| `Ctrl + T` | Open Stats |
| `Ctrl + S` | Save |
| `Ctrl + A` | Add task |
| `Ctrl + R` | Remove task |
| `Ctrl + E` | Edit task |

---

## 12. Troubleshooting

| Problem | Possible solution |
|--------|-------------------|
| App does not start | Check Java version (`java -version`), Java 17+ required. |
| Data not loading/saving | Ensure `data/` exists and is writable. |
| Server unavailable | Wait briefly (server wake-up) and retry, or use offline mode. |
| Google login fails | Ensure a default browser is installed and working. |
| No tray notifications | Check whether your OS supports a system tray. |
| Chart export fails | Ensure target folder is writable. |

---

## 13. Data Storage Overview

- **Local database:** `data/habittracker.db` (SQLite)
- **User text files:** `tasks.txt` and `user.txt` in the user directory
- **Avatar (offline):** `data/avatar-profile.properties`
- **Backups:** `data/cloud-sync/`

When logged in, tasks and profile are additionally saved on the server.
