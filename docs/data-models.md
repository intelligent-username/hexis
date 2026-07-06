# Data models

Domain types live in `shared:core` and have no platform dependencies. Types are annotated `@Serializable` where they cross a serialization boundary (backup, DataStore, or navigation).

## Habits

### `Habit`

| Field | Type | Description |
|---|---|---|
| `id` | `Long` | Auto-generated primary key |
| `title` | `String` | Display name |
| `description` | `String` | Optional notes |
| `time` | `LocalDateTime` | Reminder time |
| `days` | `Set<DayOfWeek>` | Days the habit is scheduled |
| `index` | `Int` | Manual sort order |
| `reminder` | `Boolean` | Whether an alarm is active |
| `displayMode` | `DisplayMode` | `CHECKBOX` or `PROGRESS` |
| `targetValue` | `Double?` | Completion threshold for progress mode |
| `incrementBy` | `Double` | Value added per tap in progress mode |
| `pomodoroLinked` | `Boolean` | Whether completed Pomodoro sessions count toward this habit |

`DisplayMode` controls how the habit behaves each day:
- `CHECKBOX` — done or not done. No partial state.
- `PROGRESS` — accumulates a numeric value toward `targetValue`. Each tap adds `incrementBy`.

### `HabitStatus`

One row per (habit, date) pair.

| Field | Type | Description |
|---|---|---|
| `id` | `Long` | Primary key |
| `habitId` | `Long` | FK → `Habit.id` |
| `date` | `LocalDate` | The day this record covers |
| `value` | `Double` | Current progress. `0.0` = untouched; `≥ targetValue` = complete |

### `HabitWithAnalytics`

A view object computed by the repository on each emission. Never persisted.

| Field | Type | Description |
|---|---|---|
| `habit` | `Habit` | Source habit |
| `consistency` | `Float` | Ratio of completed days to scheduled days (0–1) |
| `statuses` | `List<HabitStatus>` | Full status history |
| `weeklyComparisonData` | `List<Double>` | Per-week totals for the comparison chart |
| `weekDayFrequencyData` | `Map<String, Int>` | Completion count by day name for the frequency chart |
| `currentStreak` | `Int` | Consecutive scheduled days completed through today |
| `bestStreak` | `Int` | All-time best streak |
| `startedDaysAgo` | `Long` | Days since the first status record |

### `OverallAnalytics`

Cross-habit aggregate emitted by `HabitRepo.getOverallAnalytics()`.

| Field | Type | Description |
|---|---|---|
| `heatMapData` | `Map<LocalDate, Int>` | Total completions per day, used for the calendar heat map |
| `weekDayFrequencyData` | `Map<String, Int>` | Completion counts by day name, aggregated across all habits |
| `completedHabits` | `Pair<LocalDate, List<String>>?` | Most recent day with at least one completion, plus the habit names |
| `consistency` | `Float` | Global consistency across all habits |
| `topHabits` | `List<HabitRanking>` | Habits sorted by consistency |

### `TimeDivision`

A named time block for grouping habits on the list (e.g. "Morning", "Evening"). Stored in DataStore, not the database.

| Field | Type | Description |
|---|---|---|
| `id` | `Long` | Stable identifier |
| `name` | `String` | Display label |
| `index` | `Int` | Sort order |

Each habit can be assigned to at most one `TimeDivision`. The mapping (`habitId → divisionId`) is stored in `SettingsDatastore.getHabitTimeDivisionMap()`.

---

## Tasks

### `Task`

| Field | Type | Description |
|---|---|---|
| `id` | `Long` | Auto-generated primary key |
| `categoryId` | `Long` | FK → `Category.id` |
| `title` | `String` | Task text |
| `index` | `Int` | Sort order within its category |
| `status` | `Boolean` | Completed flag |
| `reminder` | `LocalDateTime?` | Optional one-shot reminder alarm |

### `Category`

| Field | Type | Description |
|---|---|---|
| `id` | `Long` | Auto-generated primary key |
| `name` | `String` | Display name |
| `color` | `Int` | ARGB color int from `CategoryColors` |

The repository exposes tasks as `Flow<Map<Category, List<Task>>>`, so they arrive pre-grouped.

---

## Repository interfaces

### `HabitRepo`

| Method | Return | Notes |
|---|---|---|
| `upsertHabit(habit)` | `suspend` | Insert or update |
| `deleteHabit(habitId)` | `suspend` | Cascades to statuses |
| `getHabits()` | `suspend List<Habit>` | One-shot snapshot |
| `getHabitsWithAnalytics()` | `Flow<List<HabitWithAnalytics>>` | Analytics recomputed on each emission |
| `getHabitsWithStatus()` | `Flow<List<Pair<Habit, Boolean>>>` | Today's completion state per habit |
| `getCompletedHabitIds()` | `Flow<List<Long>>` | IDs of habits completed today |
| `getOverallAnalytics()` | `Flow<OverallAnalytics>` | Aggregate analytics |
| `insertHabitStatus(status)` | `suspend` | Mark a habit done or progressed |
| `deleteHabitStatus(habitId, date)` | `suspend` | Un-mark a day |
| `incrementHabitProgress(...)` | `suspend Double` | Returns the new value |
| `decrementHabitProgress(...)` | `suspend Double` | Returns the new value |
| `observePomodoroLinkedHabits()` | `Flow<List<Habit>>` | Habits with `pomodoroLinked = true` |
| `isHabitCompleted(habitId, date)` | `suspend Boolean` | Quick completion check |

### `TaskRepo`

| Method | Return | Notes |
|---|---|---|
| `getTasksFlow()` | `Flow<Map<Category, List<Task>>>` | Tasks grouped by category |
| `getCompletedTasksFlow()` | `Flow<List<Task>>` | Completed tasks only |
| `upsertTask(task)` | `suspend` | Insert or update |
| `deleteTask(task)` | `suspend` | |
| `upsertCategory(category)` | `suspend` | |
| `deleteCategory(category)` | `suspend` | Does not cascade-delete tasks |
| `updateTaskIndexById(id, index)` | `suspend` | Called during drag-to-reorder |

### `SettingsDatastore`

All settings go into a single DataStore `Preferences` file. Getters return `Flow`; setters are `suspend`.

| Preference | Type | Default |
|---|---|---|
| Start of week | `DayOfWeek` | Monday |
| Starting section | `Sections` | Tasks |
| 24-hour time | `Boolean` | false |
| Notifications enabled | `Boolean` | true |
| Biometric lock | `Boolean` | false |
| Task reorder mode | `Boolean` | false |
| Compact view | `Boolean` | false |
| Archived habit IDs | `Set<Long>` | empty |
| Time divisions | `List<TimeDivision>` | empty |
| Habit→division map | `Map<Long, Long>` | empty |
| Pomodoro settings | `PomodoroSettings` | 25 / 5 / 15 min, interval 4 |
