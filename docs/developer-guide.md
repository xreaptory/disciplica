# Disciplica Developer Guide (v1.0)

## Architecture

## Layers

- `View` (JavaFX UI): `src/main/java/View`
- `Controller`: `MainController`
- `Domain`: `model/domain/model`
- `Persistence`:
  - SQLite repository
  - JPA/Spring Data modules
- `Services`:
  - user service
  - gamification
  - reminders
  - backup scheduler
  - portability/import-export

## Dependency Injection

Guice module:
- `src/main/java/model/di/AppModule.java`

Primary boot path:
- `model.bootstrap.Main`
- `model.bootstrap.HabitTrackerApp`

## Build & Run

Requirements:
- Java 17
- Maven wrapper

Commands:

```bash
./mvnw clean test
./mvnw -DskipTests package
./mvnw javafx:run
```

## Packaging (JAR)

```bash
./mvnw -DskipTests package
```

Output:
- shaded runnable JAR under `target/`

## CI / Release

- CI workflow: `.github/workflows/ci.yml`
- Release workflow: `.github/workflows/release.yml`

## Localization

Resource bundles:
- `src/main/resources/i18n/messages_en.properties`
- `src/main/resources/i18n/messages_de.properties`

## Styling

Global theme CSS:
- `src/main/resources/css/habitica-theme.css`

## Performance Notes

- paged completion history loading
- additional SQL indexes
- short-lived user stats cache
- lazy background scheduler initialization

See:
- `docs/optimization-guide.md`

## Data Portability

- encrypted JSON export/import
- CSV export
- cloud-sync-ready local folder
- scheduled backups

Key class:
- `model.service.DataPortabilityService`

## Memory Diagnostics

Heap dump utility:
- `model.service.MemoryDiagnosticsService`
