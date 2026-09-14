# Ἕξις ⟹ Ἀρετή

![Hexis Banner](fastlane/metadata/android/en-US/images/banner.png)

I made this app in hopes of improving my own habits, since none of the other habit trackers were comprehensive enough for me. The goal was to make it both functional and pleasant to use. I also wanted to try out some new ideas. The inspiration for the app is from the cliché saying of "Excellence is not an act but a habit", inspired from Aristotle. The word he uses for habit is "Ἕξις", pronounced "Hexis", which is what the appis named after. It's supposed to be the habit builder that helps you lead yourself to excellence, Ἀρετή.

## Features

![Features Banner](fastlane/metadata/android/en-US/images/features-banner.png)

- Habit tracking: binary and quantity-based, pomodoro-linked, with reminders.
- Tasks with categories, descriptions, and importing.
- Notes: written into a `Tasks` sub-tab. Several custom types, including markdown, counting tables, timelined journals, and vaults (password-accessed).
- Analytics: streaks, weekly charts, heat maps, and consistency scores.
- Widgets: habit overview, streak display, week chart, progress analytics, pomodoro focus, tasks list, and more, to remind you of what you need to do today.
- Backup and restore
- Material You theming.
- No ads, trackers, free-version limitations, or anything of that sort.

This is probably the most comprehensive habit tracker you can find, and everything is designed with full focus on function. Thanks to Claude for the nice UI.

## Usage

> Note that, for now, Hexis is Android-only. If I get enough request, I'll consider making a iOS version.

### Download

Download the latest APK from the [release page](https://github.com/intelligent-username/hexis/releases).

Find the file you downloaded and click on it to run it. Android will prompt with a security warning since we're not downloading from the Play Store. Just click on "Install Anyway" and you should be good to go.

### Build from source

If you want the build the app yourself, run this command:

In general: 

```shell
./gradlew assembleRelease
```

On Windows:

```bat
gradlew.bat assembleRelease
```

If you want to modify the app and build it yourself, take the following steps. Note that you can't download the APK(s) that I've uploaded and then update them with your own build since the signing keys are different (for security reasons), if you try to download anyway it'll create two versions of the same app.

#### Prerequisites

- JDK 21
- Android SDK (compileSdk 37 / targetSdk 37)
- Android Studio or Gradle 9.5.1+

#### Editing

Open the application in Android Studio or VSCode (or with your text editor of choice) and make the changes you want. After you're done, just run the `assembleRelease` task.

The APK lands at `androidApp/build/outputs/apk/release/`.

#### Signing and updates

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

This project is licensed under the GNU General Public License v3.0. See [LICENSE](LICENSE) for more information.
