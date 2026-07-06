# Architecture

## Modules

Four Gradle modules. Dependencies point inward: `androidApp` depends on `shared:ui` depends on `shared:core`. `webDemo` depends on `shared:ui`.

| Module | Path | Role |
|---|---|---|
| `:shared:core` | `shared/core/` | Pure KMP domain layer. No platform dependencies. |
| `:shared:ui` | `shared/ui/` | Compose Multiplatform UI and ViewModels. Shared across all targets. |
| `:androidApp` | `androidApp/` | Android app. Repositories, Room databases, widgets, platform implementations. |
| `:webDemo` | `webDemo/` | WASM/JVM demo entry point. Thin shell. |

## Layers

```
shared:core  (domain)
    |
shared:ui    (presentation)
    |
androidApp   (data / platform)
```

The domain layer knows nothing about Compose, Android, or any platform API. The UI layer depends on domain models and interfaces. The data layer (`androidApp`) provides concrete implementations.

## Key patterns

### State + Action (MVI-like)

Each screen has a `State` data class and an `Action` sealed interface. ViewModels expose `state: StateFlow<XState>` and `onAction(XAction)`. Composables collect state with `collectAsStateWithLifecycle()` and dispatch actions.

| Screen | State | Action | ViewModel |
|---|---|---|---|
| Tasks | `TaskState` | `TaskAction` | `TasksViewModel` |
| Habits | `HabitState` | `HabitsAction` | `HabitViewModel` |
| Settings | `SettingsState` | `SettingsAction` | `SettingsViewModel` |
| App-level | `MainAppState` | — | `MainViewModel` |

### Repository pattern

Interfaces (`XRepo`) live in `shared:core`. Implementations (`XRepository`) live in `androidApp` and are bound with `@Single(binds = [...])` via Koin.

### Platform abstraction

`expect` / `actual` declarations handle platform-specific code: themes, date pickers, alarm scheduling, biometrics, and data stores.

### Reactive data

Room DAOs return `Flow`. Repositories combine multiple flows with `combine()`. ViewModels expose results as `StateFlow` via `stateIn()`.

### Navigation

JetBrains Navigation 3 with `@Serializable` sealed interface `AppSections: NavKey`. Three tabs: Tasks, Habits, Settings. The layout adapts: bottom navigation on compact screens, a navigation rail on expanded.

## Data flow

```
User taps UI
  -> Composable calls onAction(action)
    -> ViewModel.onAction()
      -> ViewModel calls repo.suspendFun() or _state.update { }
        -> StateFlow emits
          -> Composable re-renders via collectAsStateWithLifecycle()
```

## Project layout

### `shared:core/src/commonMain/`

| Path | Contents |
|---|---|
| `habits/` | `Habit`, `HabitStatus`, `HabitWithAnalytics`, `OverallAnalytics`, `HabitRepo` interface |
| `tasks/` | `Task`, `Category`, `PomodoroSession`, `PomodoroSettings`, `TaskRepo` / `PomodoroRepo` interfaces |
| `interfaces/` | Platform abstraction interfaces: `SettingsDatastore`, `ThemeDatastore`, `AlarmScheduler`, `BiometricUtils`, `PomodoroAlarm`, `VibratorUtil` |
| `settings/` | `Sections` enum |
| `theme/` | `AppTheme`, `PaletteStyle`, `Fonts` enums; `Theme` data class |
| `app/` | Version info |

### `shared:ui/src/commonMain/`

| Path | Contents |
|---|---|
| `viewmodel/` | `TasksViewModel`, `HabitViewModel`, `SettingsViewModel`, `MainViewModel` |
| `app/` | `MainApp` root composable, `AppSections`, `MainAppState` |
| `task/` | `TaskState`, `TaskAction`, `TasksPage`, `TaskList`, `TaskUpsertSheet` |
| `habit/` | `HabitState`, `HabitsAction`, `HabitsList`, `Calendar`, `CalendarHeatMap`, `AnalyticsPage`, `OverallAnalytics`, `HabitCard` |
| `setting/` | `SettingsGraph`, pages for root, look & feel, UX, backup, about, and changelog |
| `components/` | `HexisDialog`, `HexisBottomSheet`, `HexisTimePicker`, `ColorPickerDialog`, `ExpressiveSwitch`, `Empty`, `PageFill` |
| `theme/` | `HexisTheme` (expect), `AnimatedColorScheme`, `ProvideTypography` |

### `androidApp/src/main/`

| Path | Contents |
|---|---|
| `app/` | `MainActivity`, `HexisApplication` |
| `di/` | Koin module wiring |
| `habits/data/` | Room entities, DAOs, database, `HabitRepository` |
| `tasks/data/` | Room entities, DAOs, database, `PomodoroRepository`, `TasksRepository` |
| `core/data/` | DataStore, notifications, backup, alarm, boot receiver |
| `widgets/` | Four Glance home screen widgets |

## Coding conventions

- Formatting: ktfmt (Kotlinlang style) via Spotless. Run `./gradlew spotlessApply` before committing.
- GPL v3 copyright header in every `.kt` file.
- Koin annotations: `@Single`, `@KoinViewModel`, `@Provided`, `@Module`.
- State: `MutableStateFlow` + `_state.update { }` + `.stateIn()`.
- No comments unless the logic is genuinely non-obvious.
- Expose `Modifier` on composables.
- Prefer `combine()` over nested collectors for multiple flows.
- All new code should be multiplatform (`shared:core` or `shared:ui`) unless Android-only is explicitly required.

## Build system

- Gradle 9.5.1, Kotlin 2.4.0, Compose Multiplatform 1.11.1
- Room 3.0.0-rc01, Koin 4.2.2, kotlinx-serialization, kotlinx-datetime
- Version catalog: `gradle/libs.versions.toml`
- Two Android flavors: `play` (RevenueCat billing) and `foss`
- Three build types: `debug`, `beta`, `release`
- Web demo: `./gradlew :webDemo:hotRunJvm --auto` for hot reload on JVM
