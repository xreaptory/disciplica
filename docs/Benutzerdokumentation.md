# Disciplica – Benutzerdokumentation (Version 1.0)

Disciplica ist eine Gewohnheits- und Aufgabenverwaltung im Stil eines Rollenspiels.
Für erledigte Aufgaben erhält man Erfahrungspunkte (XP), Gold und steigt im Level
auf; versäumte Aufgaben kosten Lebenspunkte. Optional lassen sich die Daten über
einen Server mit anderen teilen (Gruppen, Chat).

---

## 1. Systemvoraussetzungen

- **Betriebssystem:** Windows, macOS oder Linux
- **Java:** Version 17 oder neuer (JDK oder JRE)
- **Internet:** nur für die Online-Funktionen (Anmeldung am Server, Gruppen, Google-Login)
  erforderlich. Der Offline-Modus funktioniert ohne Internet.

Ob Java installiert ist, lässt sich in einer Eingabeaufforderung bzw. einem Terminal prüfen:

```
java -version
```

Wird eine Version 17 oder höher angezeigt, ist alles bereit.

---

## 2. Installation und Start

Die Anwendung wird als **ausführbare JAR-Datei** ausgeliefert. Eine Installation ist
nicht nötig.

1. Die Datei `disciplica-client-1.0-SNAPSHOT-consumer.jar` in einen beliebigen Ordner legen.
2. Die Anwendung starten:

   ```
   java -jar disciplica-client-1.0-SNAPSHOT-consumer.jar
   ```

   Unter Windows kann die JAR-Datei meist auch per Doppelklick gestartet werden,
   sofern Java korrekt eingerichtet ist.

Beim ersten Start legt die Anwendung im Unterordner `data/` automatisch eine lokale
Datenbank an. Dieser Ordner muss beschreibbar sein.

---

## 3. Anmeldung

Nach dem Start erscheint das Anmeldefenster mit drei Möglichkeiten:

| Möglichkeit | Beschreibung |
|-------------|--------------|
| **Anmelden / Registrieren** | Anmeldung mit E-Mail und Passwort. Über den Link unten lässt sich zwischen Anmeldung und Registrierung wechseln. Bei der Registrierung wird zusätzlich ein Benutzername benötigt. |
| **Continue with Google** | Anmeldung über ein Google-Konto. Es öffnet sich der Standardbrowser; nach erfolgreicher Anmeldung kehrt man zur Anwendung zurück. |
| **Continue Offline** | Start ohne Server. Alle Daten werden ausschließlich auf diesem Computer gespeichert. Gruppen- und Google-Funktionen stehen dann nicht zur Verfügung. |

**Hinweis zum Server:** Der gehostete Server kann beim ersten Aufruf bis zu ca. 60
Sekunden zum „Aufwachen“ benötigen. In dieser Zeit wird ein entsprechender Hinweis
angezeigt – bitte einfach warten.

**Eingaberegeln bei der Registrierung:**

- Benutzername: 3 bis 32 Zeichen
- E-Mail: gültiges Format (z. B. `name@beispiel.at`)
- Passwort: 10 bis 128 Zeichen

---

## 4. Aufbau des Hauptfensters

Am linken Rand befindet sich die Navigationsleiste mit vier Bereichen:

- **Dashboard** – Diagramme und Auswertungen
- **Habits** – Gewohnheiten und Aufgaben verwalten
- **Stats** – Spielerwerte, Avatar und Shop
- **Party** – Gruppe, Mitglieder und Chat

Der jeweils aktive Bereich wird hervorgehoben. Der Hauptbereich rechts zeigt den
Inhalt des gewählten Bereichs.

---

## 5. Gewohnheiten verwalten (Bereich „Habits“)

Im Bereich **Habits** werden Aufgaben angelegt und gepflegt.

### 5.1 Aufgabe hinzufügen

1. **Name**, **Beschreibung** und **Punkte** eingeben.
2. Die Art der Aufgabe auswählen:
   - **Daily Habit** – tägliche Gewohnheit (führt eine Serie/Streak)
   - **Weekly Habit** – wöchentliche Gewohnheit (höherer Serienbonus)
   - **OneTimeTask** – einmalige Aufgabe (To-do)
3. Auf **Add** klicken. Die Aufgabe erscheint in der Liste.

### 5.2 Aufgabe ändern oder entfernen

1. In der Liste einen Eintrag auswählen – die Felder werden automatisch befüllt.
2. Werte anpassen und auf **Change** klicken, um die Aufgabe zu ändern,
   oder auf **Remove**, um sie zu löschen.

### 5.3 Aufgabe abschließen

1. In der Liste auf eine Aufgabe **doppelklicken** – es öffnet sich ein Detailfenster.
2. Auf **Complete** klicken.

Beim Abschließen erhält man Punkte (XP) und Gold; bei täglichen und wöchentlichen
Gewohnheiten erhöht sich zusätzlich die Serie. Einmalige Aufgaben werden nach dem
Abschließen aus der Liste entfernt.

### 5.4 Speichern

Über **Save** werden die Daten gespeichert. Im angemeldeten Zustand erfolgt die
Speicherung automatisch auf dem Server.

---

## 6. Dashboard (Bereich „Dashboard“)

Das Dashboard fasst den Fortschritt grafisch zusammen:

- **Erfüllungsquote** im gewählten Zeitraum
- **Stärke nach Kategorie**
- **Rangliste der aktuellen Serien**
- **XP-Verlauf**

Über das Auswahlfeld lässt sich der Zeitraum einstellen (z. B. letzte 7 Tage,
letzte 30 Tage, letztes Jahr). Mit **Export Charts** kann das Dashboard als
Bilddatei (PNG) gespeichert werden.

---

## 7. Spielerwerte, Avatar und Shop (Bereich „Stats“)

Im Bereich **Stats** sieht man die wichtigsten Werte und gestaltet seinen Avatar:

- **Level, XP, Gold und Lebenspunkte** sowie Fortschrittsbalken
- **Avatar:** Körpergröße, Haut-, Haar- und Kleidungsfarbe sowie Frisur lassen sich
  über die Auswahlfelder anpassen. Die Vorschau wird sofort aktualisiert.
- **Shop:** Mit gesammeltem Gold können Ausrüstungsgegenstände (z. B. Rüstung,
  Kopfbedeckung, Waffe) gekauft und am Avatar angelegt werden.

Erreicht man ein neues Level, wird dies durch eine kurze Animation angezeigt.

---

## 8. Gruppen und Chat (Bereich „Party“)

> Verfügbar nur im angemeldeten Zustand (nicht im Offline-Modus).

Im Bereich **Party** kann man:

- eine **Gruppe erstellen** (man wird automatisch deren Leiter),
- als Leiter andere Personen über **Benutzername oder E-Mail einladen**,
- Einladungen **annehmen oder ablehnen**,
- mit den Mitgliedern im **Chat** in Echtzeit schreiben.

---

## 9. Erinnerungen (System-Tray)

Disciplica blendet ein Symbol im System-Tray (Infobereich) ein und erinnert einmal
täglich an noch offene Gewohnheiten. Über das Kontextmenü des Tray-Symbols stehen
zur Verfügung:

- **Check now** – sofort prüfen
- **Snooze 10 minutes** / **Snooze 30 minutes** – Erinnerung aufschieben
- **Disable reminders** – Erinnerungen abschalten

> Hinweis: Erfordert ein Betriebssystem mit Tray-Unterstützung.

---

## 10. Sicherung, Wiederherstellung und Export

- **Verschlüsselte Sicherung:** Die Benutzerdaten können als verschlüsselte
  JSON-Datei gesichert und wieder eingelesen werden. Automatische Sicherungen
  werden – je nach Einstellung täglich oder wöchentlich – im
  Synchronisierungsordner (`data/cloud-sync/`) abgelegt.
- **Verschlüsselungsschlüssel:** Über die Umgebungsvariable `DISCIPLICA_BACKUP_KEY`
  lässt sich ein eigenes Passwort für die Verschlüsselung festlegen. Ist keines
  gesetzt, wird ein Entwicklungs-Standardschlüssel verwendet (nicht für den
  produktiven Einsatz empfohlen).
- **CSV-Export:** Die Gewohnheiten können als CSV-Datei exportiert werden, etwa zur
  Auswertung in einer Tabellenkalkulation.

---

## 11. Tastenkürzel

| Kürzel | Aktion |
|--------|--------|
| `Strg + D` | Dashboard öffnen |
| `Strg + H` | Habits öffnen |
| `Strg + T` | Stats öffnen |
| `Strg + S` | Speichern |
| `Strg + A` | Aufgabe hinzufügen |
| `Strg + R` | Aufgabe entfernen |
| `Strg + E` | Aufgabe ändern |

---

## 12. Fehlerbehebung

| Problem | Mögliche Lösung |
|---------|------------------|
| Anwendung startet nicht | Java-Version prüfen (`java -version`), es wird Java 17 oder neuer benötigt. |
| Daten werden nicht geladen/gespeichert | Sicherstellen, dass der Ordner `data/` vorhanden und beschreibbar ist. |
| Server nicht erreichbar | Einen Moment warten (der Server „wacht auf“) und erneut versuchen, oder den Offline-Modus verwenden. |
| Google-Anmeldung schlägt fehl | Prüfen, ob ein Standardbrowser vorhanden ist; die Anmeldung läuft über den Browser. |
| Keine Tray-Benachrichtigungen | Prüfen, ob das Betriebssystem einen System-Tray unterstützt. |
| Diagramm-Export schlägt fehl | Sicherstellen, dass der Zielordner beschreibbar ist. |

---

## 13. Datenspeicherung im Überblick

- **Lokale Datenbank:** `data/habittracker.db` (SQLite)
- **Textdateien des Benutzers:** `tasks.txt` und `user.txt` im Benutzerverzeichnis
- **Avatar (offline):** `data/avatar-profile.properties`
- **Sicherungen:** `data/cloud-sync/`

Im angemeldeten Zustand werden Aufgaben und Profil zusätzlich auf dem Server gespeichert.
