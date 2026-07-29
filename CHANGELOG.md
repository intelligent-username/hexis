# Changelog

## 1.6.4: Home Widgets & Habit Reordering Fixes

- Instant real-time updates for notes, tasks, and habit widgets
- Uncapped counting table previews on home widgets
- Smooth habit reordering in move mode with zero drag jitter
- Enabled Kotlin compiler daemon for faster incremental builds

## 1.6.3: Pomodoro Persistence & Tasks UI Refinement

- Pomodoro timer background persistence: Wall-clock target tracking and alarm triggers ensure zero lost focus time when switching tabs, leaving the app, or with screen locked
- Reactive Pomodoro preview: Real-time database updates for instant today stats (session count & minutes) refresh
- Auto-collapsing Add Task FAB: Task creation button dynamically shrinks by 50% while scrolling down to keep tasks readable
- Soft keyboard IME inset padding: Automatic keyboard IME padding in habit and task sheets keeps input details visible while typing

## 1.6.2: Pomodoro & Analytics Improvements

- Pomodoro now tracks total time worked and per-day session counts in Session History
- Pomodoro heat map now logs evening & partial sessions accurately to the current day
- Progression trends chart now displays the exact week of the year
- Habit creation now auto-capitalizes title words and shows inline validation badges
- Progress widget preview restored in widget picker

## 1.6.1: Bug fixes

- Miscellaneous bug and UI fixes

## 1.6.0: Single Note Widget, Reactive Refresh & Editor Polishing

2 New Widgets
  - Single Note pin: Select and display any note (Markdown, Counting Table, or Journal) on your home screen with per-widget configuration.
  - All notes shortcut view
- Refined Note Editor Rendering
- Refined note previews
- Habit streaks points adjustment, retroactive counter habit changing, and other UX improvements.

## 1.3.0: Drag Reordering, Shortcuts & UX Refinements

- New Note type: TABLE counters
- Improved Note Gestures: Reordering notes is now smoother than ever. Long-pressing without dragging seamlessly toggles selection mode, and action buttons are guarded while selecting notes to prevent accidental archiving or pinning. Also viewable as two columns now.
- Enhanced Horizontal Rules: Tripled the visual length of horizontal dividers (`---`) in markdown notes for clearer document structure.
- Reorganized Settings
- Fixed tons of UI mistakes

## 1.2.3: Analytics & QoL

- New Progress Analytic
- Habit reordering setting
- Improved notes. No longer as
- General UI stuff

## 1.1.1: Balancing

- New notes feature in Tasks
- UI improvements
- Bug fixes like streak detection, week switching, analytics mismatch
- Compiling optimzations for developers
- Improved database migrations and thoroughness
- Improved pomodoro calculations
- Improved imports and exports

## 1.0.0: Initial Release

### Core Features

- Binary and quantity-based habit tracking
- Categorizable tasks
- Pomodoro timer
- Pomodoro-based habits
- Habit reminders
- Categorization and filtering
- Analytics and progress tracking
- Widgets
- Data export and import
- Beautiful UI :)
