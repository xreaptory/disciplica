# Performance Notes

## Bottleneck identified

The slowest repeated path in data access was user-id resolution in `SQLiteHabitRepository`:

- Querying `SELECT id FROM users WHERE username = ?` repeatedly in hot repository operations.
- This added extra database round-trips before actual habit queries.

## Optimization applied

- Added `ConcurrentMap<String, Long> userIdCache` in `SQLiteHabitRepository`.
- `resolveUserId(User user)` now returns cached ids when available.
- Cache is updated on both select-hit and insert-created user paths.

## Profiling workflow

### VisualVM

1. Start app with profiling-friendly flags:
   - `mvn javafx:run`
2. Open VisualVM and attach to the Java process.
3. Use CPU sampler while running habit list and completion flows.
4. Confirm reduced time spent in JDBC calls from `resolveUserId`.

### async-profiler

1. Start app and note PID.
2. Run:
   - `profiler.sh -d 20 -e cpu -f profile.html <PID>`
3. Open `profile.html` and inspect hot frames under repository JDBC query paths.

