# Ἕξις ⟹ Ἀρετή

![Hexis Banner](fastlane/metadata/android/en-US/images/banner.png)

## Features

- Robust multi-kind habit tracking
- Tasks and pomodoro features
- Analytics
- Reminders
- Widgets

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests.

## Usage

### By Downloading

Navigate to the [release page](https://github.com/intelligent-username/hexis/releases) and download the latest APK.

### By Building

#### Prerequisites

- JDK 21
- Android SDK (compileSdk 37 / targetSdk 37)
- Android Studio (recommended) or Gradle 9.5.1+

##### Generating the APK

```shell
# FOSS variant (no Google Play dependencies)
./gradlew assembleFossRelease
```

On Windows:

```bat
gradlew.bat assembleFossRelease
```

The APK will be at `androidApp/build/outputs/apk/foss/release/`.

## Signing & Updates

By default, running `./gradlew assembleFossRelease` will compile the app and sign it with a default debug key. 

**Important Note on Updating:** Android requires that any app update is signed with the exact same cryptographic key as the currently installed version. Because the official keystore is kept private, any version you build locally will have a different signature than the official releases downloaded from GitHub. 

If you want to install your custom-built version on a device that already has the official app installed, you must **uninstall the official version first** (make sure to use the in-app backup feature to save your data beforehand!) since the private keys will not match.

For **custom** signing, Hexis's CI signs builds via injected Gradle properties. If you wish to sign your own builds with your own keystore locally, you can create one and pass the properties like so:

```shell
./gradlew assembleFossRelease \
  -Pandroid.injected.signing.store.file=/path/to/keystore.jks \
  -Pandroid.injected.signing.store.password=storepass \
  -Pandroid.injected.signing.key.alias=key0 \
  -Pandroid.injected.signing.key.password=keypass
```
