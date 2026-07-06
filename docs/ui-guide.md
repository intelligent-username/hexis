# UI Guide

## Architecture

The UI uses Compose Multiplatform shared across Android, JVM desktop, and WebAssembly. Everything in `shared:ui` targets all platforms. Platform-specific code lives in platform source sets and uses the `expect` / `actual` pattern.

## Theme System

### Core model (`shared:core/.../theme/`)

The `Theme` data class holds all theming state:

| Field | Type | Default | Description |
|---|---|---|---|
| `appTheme` | `AppTheme` | `SYSTEM` | Light, Dark, or follow system |
| `isAmoled` | `Boolean` | `false` | True black for OLED screens |
| `isMaterialYou` | `Boolean` | `false` | Use Material You dynamic color on Android 12+ |
| `font` | `Fonts` | `FIGTREE` | Typeface selection |
| `paletteStyle` | `PaletteStyle` | `TONALSPOT` | Color palette algorithm |
| `seedColor` | `Int` | `0xFFFFFF` | Base color for palette generation |

### Palette styles

Available in `PaletteStyle` enum: TONALSPOT, NEUTRAL, VIBRANT, EXPRESSIVE, RAINBOW, FRUITSALAD, MONOCHROME, FIDELITY, CONTENT.

### Fonts

Available in `Fonts` enum: POPPINS, INTER, MANROPE, MONTSERRAT, FIGTREE, OUTFIT, GOOGLE_SANS, SYSTEM_DEFAULT.

The app also uses Google Sans Flex for UI emphasis (variable font with weight, slant, and roundness axes).

### Theme application

`HexisTheme` is an `expect` composable in `shared:ui/theme/`. Each platform provides an `actual` implementation:
- Android: Material 3 `MaterialTheme` with dynamic color support
- JVM: Material 3 `MaterialTheme`
- WASM: Material 3 `MaterialTheme`

The `AnimatedColorScheme` extension function wraps every color slot in `animateColorAsState` (400ms tween) for smooth theme transitions.

### Typography

Defined in `ProvideTypography.kt`. Uses Material 3 type scale with a user-selectable font family applied across all 15 text styles (display, headline, title, body, label).

## Components

Reusable composables in `shared:ui/components/`:

| Component | Purpose |
|---|---|
| `HexisBottomSheet` | Modal bottom sheet, max width 500dp, skip partially expanded |
| `HexisDialog` | Alert/confirmation dialog |
| `HexisTimePicker` | Time picker (platform actual) |
| `ExpressiveSwitch` | Switch with check/close icon thumb |
| `ColorPickerDialog` | Color picker for task categories |
| `Empty` | Empty state placeholder |
| `PageFill` | Full-page fill spacer |
| `FossPaywall` | Upsell for FOSS flavor features |
| `ChangelogSheet` | Changelog display bottom sheet |
| `InitialLoading` | Splash loading state |
| `ListItemExt` | Extended list item layout |

## Navigation

`AppSections` sealed interface with `@Serializable` annotations:

```
Tasks -> habits list page
Habits -> tasks list page
Settings -> settings root page
```

Adaptive layout: `BottomNavigation` on compact screens, `NavigationRail` on expanded. The navigation graph is defined in `HabitsGraph.kt`, `SettingsGraph.kt`, and the tasks graph lives inline in the main scaffold.

## Screen Layout Rules

- Each section has its own `State` + `Action` pair.
- Views never call the repository directly. They go through the ViewModel's `onAction()` method.
- Composables use `collectAsStateWithLifecycle()` for lifecycle-aware collection.
- All top-level page composables accept a `Modifier` parameter.
- Bottom sheets use `HexisBottomSheet` for consistent styling.

## String Resources

All user-facing strings come from Compose Multiplatform resource files (`composeResources/`). No hardcoded strings in composables.

## Do Not Modify Unless Asked

The UI has had extensive investment in its design. Do not change composables, component styles, shapes, colors, animations, layouts, navigation structure, spacings, typography, or string resources unless explicitly asked.
