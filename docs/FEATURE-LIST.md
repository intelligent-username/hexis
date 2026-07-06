# Feature list

## Habits

- Create habits with a title, optional description, scheduled days, and reminder time
- Two tracking modes per habit: checkbox (done/not done) or progress (numeric value toward a target)
- Progress mode: configurable target value and per-tap increment step
- Mark a habit complete or in-progress directly from the habit list
- Undo/redo completion for any day from the calendar view
- Reorder habits by drag
- Archive habits to hide them from the active list without deleting history
- Link a habit to Pomodoro sessions so completing a session auto-completes the habit
- Group habits into named time divisions (e.g. "Morning", "Evening") with custom sort order
- Per-habit reminders via Android alarm notifications

## Habit analytics

- Consistency score: ratio of completed days to scheduled days
- Current and best streak, counted over scheduled days only
- Weekly comparison chart: aggregate progress by week
- Day-of-week frequency chart: which days a habit gets done most
- Calendar view: browse and edit any past day
- Calendar heat map: color-coded completion density across dates
- Overall analytics page combining all habits:
  - Global consistency
  - Cross-habit heat map
  - Aggregate day-of-week frequency
  - Top habits ranked by consistency
  - Most recently completed habits summary

## Tasks

- Create tasks with a title, category, and optional reminder
- Organize tasks into color-coded categories
- Mark tasks complete; completed tasks move to a separate view
- Reorder tasks within a category by drag (toggled via a setting)
- Filter tasks by category
- Optional compact list view

## Pomodoro timer

- Configurable focus, short break, and long break durations
- Configurable long break interval (number of focus blocks before a long break)
- Sessions recorded to a database with start time, end time, and completion status
- Early stop records actual elapsed time
- Link a session to a habit for automatic habit completion on session end
- Today's stats: completed session count and total focus minutes
- Analytics: sessions completed per day (bar chart), sessions broken down by linked habit
- Alarm fires at session end even when the app is backgrounded

## Tasks analytics (Pomodoro)

- Daily session count chart
- Calendar of days with at least one completed session
- Session counts grouped by linked habit

## Widgets (Android home screen)

- **All tasks** — scrollable list of today's incomplete tasks; tap to complete
- **Habit overview** — all of today's habits with completion state; tap to toggle
- **Habit streak** — single habit name and current streak count
- **Habit week chart** — 7-day bar chart for a single habit

All widgets support theming via a simplified M3-compatible color scheme.

## Theming

- Light, dark, and system-follow modes
- AMOLED true-black mode
- Material You dynamic color (Android 12+)
- Nine palette styles: Tonal Spot, Neutral, Vibrant, Expressive, Rainbow, Fruit Salad, Monochrome, Fidelity, Content
- Eight font options: Poppins, Inter, Manrope, Montserrat, Figtree, Outfit, Google Sans, System Default
- Seed color picker for fully custom palettes
- 400ms animated color scheme transitions between themes

## Settings

- Start of week (any day)
- Default app section on launch
- 12/24-hour time display
- Notification enable/disable
- Biometric app lock
- Task drag-to-reorder toggle
- Compact task list view toggle
- Pomodoro timer durations and interval
- Time divisions: create, rename, reorder, delete
- Habit–division assignment

## Backup and restore

- Export all habits, tasks, categories, and statuses to a JSON file
- Import from a previously exported JSON file

## Other

- In-app changelog with version history
- Adaptive layout: bottom navigation on compact screens, navigation rail on expanded
- Multiplatform: Android app + JVM/WASM web demo sharing the same UI
- Two build flavors: `play` (with RevenueCat billing) and `foss`
