# UI guide

## Overview

The UI uses Compose Multiplatform shared across Android, JVM desktop, and WebAssembly. Everything in `shared:ui` targets all platforms. Platform-specific code lives in the appropriate source sets and uses `expect` / `actual`.

## Theme system

### Core model (`shared:core/.../theme/`)

The `Theme` data class holds all theming state:

| Field | Type | Default | Description |
|---|---|---|---|
| `appTheme` | `AppTheme` | `SYSTEM` | Light, dark, or follow system |
| `isAmoled` | `Boolean` | `false` | True black for OLED screens |
| `isMaterialYou` | `Boolean` | `false` | Dynamic color on Android 12+ |
| `font` | `Fonts` | `FIGTREE` | Typeface selection |
| `paletteStyle` | `PaletteStyle` | `TONALSPOT` | Color palette algorithm |
| `seedColor` | `Int` | `0xFFFFFF` | Base color for palette generation |

### Palette styles

`PaletteStyle` options: `TONALSPOT`, `NEUTRAL`, `VIBRANT`, `EXPRESSIVE`, `RAINBOW`, `FRUITSALAD`, `MONOCHROME`, `FIDELITY`, `CONTENT`.

### Fonts

`Fonts` options: `POPPINS`, `INTER`, `MANROPE`, `MONTSERRAT`, `FIGTREE`, `OUTFIT`, `GOOGLE_SANS`, `SYSTEM_DEFAULT`.

The app also uses Google Sans Flex for UI emphasis — a variable font with weight, slant, and roundness axes.

### Theme application

`HexisTheme` is an `expect` composable. Each platform provides an `actual` wrapping Material 3 `MaterialTheme`, with Android adding dynamic color support.

`AnimatedColorScheme` wraps every color slot in `animateColorAsState` (400ms tween) for smooth theme transitions.

### Typography

Defined in `ProvideTypography.kt`. Applies a user-selected font family across all 15 Material 3 text styles (display, headline, title, body, label).

## Components

Reusable composables in `shared:ui/components/`:

| Component | Purpose |
|---|---|
| `HexisBottomSheet` | Modal bottom sheet; max width 500dp, skips partially expanded state |
| `HexisDialog` | Alert/confirmation dialog |
| `HexisTimePicker` | Time picker (platform `actual`) |
| `ExpressiveSwitch` | Switch with check/close icon on the thumb |
| `ColorPickerDialog` | Color picker for task categories |
| `Empty` | Empty state placeholder |
| `PageFill` | Full-page fill spacer |
| `FossPaywall` | Upsell for FOSS-flavor features |
| `ChangelogSheet` | Changelog display bottom sheet |
| `InitialLoading` | Splash loading state |
| `ListItemExt` | Extended list item layout |

## Navigation

`AppSections` is a `@Serializable` sealed interface with three entries:

| Entry | Destination |
|---|---|
| `Tasks` | Task list page |
| `Habits` | Habits list page |
| `Settings` | Settings root page |

Compact screens get a bottom navigation bar; expanded screens get a navigation rail. The habits nav graph is in `HabitsGraph.kt`, settings in `SettingsGraph.kt`, and tasks inline in the main scaffold.

## Screen layout rules

- Each section has its own `State` + `Action` pair.
- Composables never call repositories directly. All mutations go through `ViewModel.onAction()`.
- Use `collectAsStateWithLifecycle()` for lifecycle-aware state collection.
- All top-level page composables accept a `Modifier` parameter.
- Bottom sheets use `HexisBottomSheet` for consistent styling.

## String resources

All user-facing strings come from Compose Multiplatform resource files under `composeResources/`. No hardcoded strings in composables.

## Do not modify unless asked

The UI has had significant investment in its design. Do not change composables, component styles, shapes, colors, animations, layouts, navigation structure, spacings, typography, or string resources unless explicitly asked.
