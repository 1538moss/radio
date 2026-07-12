# K5 Radio-cast

An Android app for browsing a grid of live radio stations and casting them to a Chromecast device via Google Cast.

## Features

- Grid of tappable station buttons, each showing the station's logo
- Cast to any Chromecast-compatible device (Google Cast SDK)
- Now Playing bar with live/buffering state
- Graceful fallback when Google Play Services / Cast isn't available on the device
- Material 3 UI in Jetpack Compose with dark/light theme and dynamic colors (Android 12+)

## Architecture

- **UI**: 100% Jetpack Compose (no XML layouts), Material 3 with dynamic theming
- **Pattern**: MVVM — `RadioViewModel` owns all Cast session state, exposed as a `StateFlow<RadioUiState>`
- **Navigation**: type-safe Navigation Compose (`@Serializable` routes)

## Requirements

- Android Studio (or a JDK 17 + Android SDK setup)
- `minSdk` 26, `targetSdk` 34, `compileSdk` 35

## Building

```bash
./gradlew assembleDebug      # unsigned debug APK
./gradlew assembleRelease     # signed release APK (needs a keystore, see below)
./gradlew test                # unit tests
```

Output APKs land in `app/build/outputs/apk/`.

## Release signing

Release builds are signed using `keystore/keystore.properties`, which is gitignored since it holds real credentials. To build a signed release locally:

1. Copy `keystore/keystore.properties.example` to `keystore/keystore.properties`.
2. Point `storeFile` at your `.jks` file (also gitignored if placed under `keystore/`) and fill in the real `storePassword`, `keyAlias`, and `keyPassword`.

Without this file present, `assembleRelease` will produce an unsigned APK.

## Project structure

```
app/src/main/java/com/svendsrud/castradio/
├── MainActivity.kt                   # entry point: edge-to-edge + setContent
├── CastOptionsProvider.kt            # Cast SDK options (receiver app id, session resume)
├── model/RadioStation.kt             # station data + hardcoded station list (incl. logo resource)
├── navigation/CastRadioNavHost.kt    # type-safe Navigation Compose graph
└── ui/
    ├── RadioViewModel.kt             # MVVM: Cast session handling + RadioUiState (StateFlow)
    ├── RadioScreen.kt                # main screen: header, station grid, snackbar
    ├── components/StationCard.kt     # station button card (status, pulse animation)
    ├── components/NowPlayingBar.kt   # bottom bar with active station + stop button
    └── theme/                        # Material 3 theme: colors, typography, dynamic color

app/src/main/res/drawable-nodpi/  # station logo bitmaps used by the app
logo/                             # original source logo files (reference/source of truth)
```
