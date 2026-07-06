# Ἕξις ⟹ Ἀρετή

![Hexis Banner](fastlane/metadata/android/en-US/images/banner.png)

## Features

- Habit tracking: binary and quantity-based, pomodoro-linked, with reminders
- Tasks with categories, pomodoro timer, and filtering
- Analytics: streaks, weekly charts, heat maps, consistency scores
- Widgets: habit overview, streak display, week chart, all tasks
- Backup and restore, Material You theming, 8 font options

## Usage

### Download

Grab the latest APK from the [release page](https://github.com/intelligent-username/hexis/releases).

### Build from source

#### Prerequisites

- JDK 21
- Android SDK (compileSdk 37 / targetSdk 37)
- Android Studio or Gradle 9.5.1+

#### Generate the APK

```shell
./gradlew assembleRelease
```

On Windows:

```bat
gradlew.bat assembleRelease
```

The APK lands at `androidApp/build/outputs/apk/release/`.

## Signing and updates

Running `./gradlew assembleRelease` compiles the app and signs it with a debug key. Android requires that app updates use the same signing key as the installed version. The official keystore is private, so any build from source will have a different signature than the GitHub releases.

To sideload your own build on a device with the official app installed, uninstall the official version first. Use the in-app backup feature to save your data before uninstalling.

Want your own signature? Create a keystore and pass it to Gradle:

```shell
./gradlew assembleRelease \
  -Pandroid.injected.signing.store.file=/path/to/keystore.jks \
  -Pandroid.injected.signing.store.password=storepass \
  -Pandroid.injected.signing.key.alias=key0 \
  -Pandroid.injected.signing.key.password=keypass
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for the code of conduct and pull request process.
