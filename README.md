# K5 Radio-cast

An Android app for browsing a list of live radio stations and casting them to a Chromecast device via Google Cast.

## Features

- List of live radio streams with per-station accent colors
- Cast to any Chromecast-compatible device (Google Cast SDK)
- Now Playing bar with live/buffering state
- Graceful fallback when Google Play Services / Cast isn't available on the device

## Requirements

- Android Studio (or a JDK 17 + Android SDK setup)
- `minSdk` 26, `targetSdk`/`compileSdk` 34

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
├── MainActivity.kt              # station list, Cast session handling, Now Playing bar
├── CastOptionsProvider.kt        # Cast SDK options (receiver app id, session resume)
├── model/RadioStation.kt         # station data + hardcoded station list
└── ui/StationsAdapter.kt         # RecyclerView adapter for the station list
```
