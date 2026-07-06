# Architecture

## Modules

Hexis has 4 Gradle modules. Dependencies point inward: `androidApp` depends on `shared:ui` depends on `shared:core`. The `webDemo` depends on `shared:ui`.

| Module | Path | Role |
|---|---|---|
| `:shared:core` | `shared/core/` | Pure KMP domain layer. Zero platform dependencies. |
| `:shared:ui` | `shared/ui/` | Compose Multiplatform UI + ViewModels. Shared across all targets. |
| `:androidApp` | `androidApp/` | Android app. Repos, Room DBs, widgets, platform implementations. |
| `:webDemo` | `webDemo/` | WASM/JVM demo entry point. Thin shell. |

## Clean Architecture Layers

```
shared:core  (domain)
    |
shared:ui    (presentation)
    |
androidApp   (data / platform)
```

The domain layer knows nothing about Compose, Android, or any platform API. The UI layer depends on domain models and interfaces. The data layer (androidApp) provides the actual implementations.

## Key Patterns

### State + Action (MVI-like)

Each screen has a State data class and an Action sealed interface. ViewModels expose `state: StateFlow<XState>` and `onAction(XAction)`. Composables collect state and dispatch actions.

| Screen | State | Action | ViewModel |
|---|---|---|---|
| Tasks | `TaskState` | `TaskAction` | `TasksViewModel` |
| Habits | `HabitState` | `HabitsAction` | `HabitViewModel` |
| Settings | `SettingsState` | `SettingsAction` | `SettingsViewModel` |
| App-level | `MainAppState` | — | `MainViewModel` |

### Repository pattern

Interfaces (XRepo) live in `shared:core`. Implementations (XRepository) live in `androidApp`. Koin binds them with `@Single(binds = [...])`.

### Platform abstraction

`expect` / `actual` declarations for platform code: themes, date pickers, alarm scheduling, biometrics, data stores.

### Reactive data

Room DAOs return `Flow`. Repos combine multiple flows with `combine()`. ViewModels expose data as `StateFlow` via `stateIn()`.

### Navigation

JetBrains Navigation 3 with `@Serializable` sealed interface `AppSections: NavKey`. Three tabs: Tasks, Habits, Settings. Adaptive layout with bottom nav on compact, nav rail on expanded.

## Data Flow

```
User taps UI
  -> Composable calls onAction(action)
    -> ViewModel.onAction()
      -> ViewModel calls repo.suspendFun() or _state.update { }
        -> StateFlow emits
          -> Composable re-renders via collectAsStateWithLifecycle()
```

## Project Layout

### `shared:core/src/commonMain/`

| Path | Contents |
|---|---|
| `habits/` | Habit domain model, HabitStatus, analytics data classes, HabitRepo interface |
| `tasks/` | Task domain model, Category, PomodoroSession, PomodoroRepo interface |
| `interfaces/` | Platform abstraction interfaces (SettingsDatastore, AlarmScheduler, BiometricUtils, etc.) |
| `settings/` | Settings sections enum |
| `theme/` | Theme data class, PaletteStyle, Fonts, AppTheme enums |
| `app/` | Version info |

### `shared:ui/src/commonMain/`

| Path | Contents |
|---|---|
| `viewmodel/` | TasksViewModel, HabitViewModel, SettingsViewModel, MainViewModel |
| `app/` | MainApp root composable, AppSections navigation, MainAppState |
| `task/` | TaskState, TaskAction, TasksPage, TaskList, TaskUpsertSheet |
| `habit/` | HabitState, HabitsAction, HabitsList, Calendar, CalendarHeatMap, AnalyticsPage, OverallAnalytics, HabitCard |
| `setting/` | SettingsGraph, settings pages (Root, LookAndFeel, Backup, About, Changelog) |
| `components/` | HexisDialog, HexisBottomSheet, HexisTimePicker, ColorPickerDialog, ExpressiveSwitch, Empty, PageFill |
| `theme/` | HexisTheme (expect), AnimatedColorScheme, ProvideTypography |

### `androidApp/src/main/`

| Path | Contents |
|---|---|
| `app/` | MainActivity, HexisApplication |
| `di/` | Koin DI module wiring |
| `habits/data/` | Room entities, DAOs, database, HabitRepository |
| `tasks/data/` | Room entities, DAOs, database, PomodoroRepository, TasksRepository |
| `core/data/` | DataStore preferences, notifications, backup, alarm, boot receiver |
| `widgets/` | 4 Glance app widgets |

## Coding Conventions

- Formatting: ktfmt (Kotlinlang style) via Spotless. Run `./gradlew spotlessApply` before committing.
- GPL v3 copyright header in every `.kt` file.
- Koin annotations: `@Single`, `@KoinViewModel`, `@Provided`, `@Module`.
- State management: `MutableStateFlow` + `_state.update { }` + `.stateIn()`.
- No comments unless the logic is genuinely non-obvious.
- Expose `Modifier` on composables.
- Prefer `combine()` over nested collectors for multiple flows.
- All new code should be multiplatform (shared:core or shared:ui) unless Android-only is explicitly required.

## Build System

- Gradle 9.5.1, Kotlin 2.4.0, Compose Multiplatform 1.11.1
- Room 3.0.0-rc01, Koin 4.2.2, kotlinx-serialization, kotlinx-datetime
- Version catalog: `gradle/libs.versions.toml`
- Two Android flavors: `play` (RevenueCat billing) and `foss`
- Three build types: `debug`, `beta`, `release`
- Web demo: `./gradlew :webDemo:hotRunJvm --auto` (hot reload on JVM)
