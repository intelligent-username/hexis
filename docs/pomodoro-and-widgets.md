# Pomodoro & widgets

## Pomodoro timer

The timer spans three layers: domain types in `shared:core`, UI state in `shared:ui`, and the Android alarm implementation in `androidApp`.

### Domain (`shared:core/tasks/`)

**`PomodoroSettings`** — timer durations, persisted in `SettingsDatastore`:

| Field | Default | Description |
|---|---|---|
| `focusMinutes` | 25 | Length of a focus block |
| `shortBreakMinutes` | 5 | Break after each focus block |
| `longBreakMinutes` | 15 | Break after every `longBreakInterval` blocks |
| `longBreakInterval` | 4 | Focus blocks before a long break |

**`PomodoroSession`** — one session record:

| Field | Description |
|---|---|
| `id` | Auto-generated primary key |
| `goalDurationMinutes` | Intended duration when started |
| `timeStarted` | Wall-clock start time |
| `timeFinished` | Null while the session is running |
| `completed` | True if the full duration elapsed |
| `timeCompletedMinutes` | Actual elapsed time; differs from the goal on early stop |
| `linkedHabitId` | FK to a habit; completing this session ticks that habit |

**`PomodoroStats`** — today's aggregate, fetched on demand:
- `completedCount` — sessions completed today
- `totalFocusMinutes` — total duration across completed sessions today

**`PomodoroRepo`**:

| Method | Notes |
|---|---|
| `insertSession(session)` | Returns the new session ID |
| `finishSession(id, timeFinished, completed, timeCompletedMinutes)` | Called on normal end or early stop |
| `getTodayStats()` | One-shot summary |
| `getCompletedDates()` | `Flow<List<LocalDate>>` — dates with at least one completed session |
| `getSessionCountsByDay()` | `Flow<List<PomodoroDayCount>>` — for the analytics bar chart |
| `getSessionCountsByHabit()` | `List<Pair<Long?, Int>>` — session counts grouped by linked habit |

### Platform (`androidApp/core/data/pomodoroalarm/`)

`PomodoroAlarmImpl` implements `PomodoroAlarm` using `AlarmManager.setExactAndAllowWhileIdle`. It sets a one-shot alarm at the session end time and cancels it if the user stops early.

`PomodoroAlarmReceiver` is the `BroadcastReceiver` that fires on alarm delivery. It posts a notification and optionally triggers haptic feedback via `VibratorUtil`.

### Habit linking

If a habit has `pomodoroLinked = true` and a session has `linkedHabitId` pointing to that habit, completing the session (i.e. `finishSession` with `completed = true`) auto-completes the habit. The repository handles this atomically.

---

## Android widgets

Widgets use Jetpack Glance. All four share `WidgetColorScheme` (a simplified M3-compatible color set that works within Glance's restricted surface) and `WidgetSize` sizing utilities.

### All tasks (`all_tasks_widget/`)

Scrollable list of today's incomplete tasks, grouped by category. Tapping a task marks it complete through `HexisIntentReceiver`. Shows an empty state when nothing is left.

### Habit overview (`habit_overview_widget/`)

All of today's scheduled habits with their completion state. Each row uses a checkbox or progress indicator depending on the habit's `DisplayMode`. Tapping toggles completion.

### Habit streak (`habit_streak_widget/`)

Compact single-habit widget. Shows the habit name and current streak count.

### Habit week chart (`habit_week_chart_widget/`)

7-day bar chart for a single habit. Each bar covers one day of the current week; bar height maps to the progress value (or 0/1 for checkbox habits).

### Intent routing

All widget taps go through `HexisIntentReceiver` (`androidApp/core/data/HexisIntentReceiver.kt`). It reads the action and extras, calls the appropriate repository method, then triggers a widget refresh. Action strings are defined in `IntentActions.kt` (`shared:core/interfaces/`).

### Widget refresh

Glance does not support observing `Flow` directly. Widgets refresh in two situations:
1. After an intent action is handled by `HexisIntentReceiver`.
2. After in-app data changes, where the repository triggers an explicit update.
