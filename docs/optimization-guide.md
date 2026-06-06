# Optimization Guide

## Memory leak analysis (heap dump)

Use `MemoryDiagnosticsService` to write a dump:

- Class: `model.service.MemoryDiagnosticsService`
- Method: `writeHeapDump(Path targetFile, boolean liveOnly)`

Recommended local verification:

1. Trigger heavy UI flows (add/remove many habits, open dashboard/stats repeatedly).
2. Write heap dump after steady state.
3. Analyze with VisualVM or Eclipse MAT.
4. Compare retained sizes for `View`, `Timeline`, and task lists.

## Slow query optimization checks

`SQLiteHabitRepository` now includes additional indexes:

- `idx_habits_user_frequency`
- `idx_habits_user_streak`
- `idx_completions_habit_completed_at`
- `idx_users_username`

Use existing query-plan helper:

- `explainQueryPlan("YOUR SQL")`

and verify index usage appears in plan details.

## Lazy loading and pagination

Habit history loading now supports paging:

- `findHabitWithCompletions(Long habitId, int pageSize, int offset)`

Use this for long habit histories to avoid loading all completion rows at once.

## Cached user stats

`MainController` includes a short-lived stats cache (`1s TTL`) consumed by `View.refreshStatsData()` to reduce repeated calculations/render pressure.

## JavaFX rendering optimization

`ListView` now uses:

- fixed cell size
- lightweight cell factory

to improve performance with large habit collections.

## Startup time

Reminder and backup schedulers are initialized lazily after first paint to reduce startup latency.
