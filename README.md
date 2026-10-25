# Cyberdeck

Personal Android launcher project. Native Kotlin + Jetpack Compose; no backend or cloud service.

## Current stage

This is the initial source/build skeleton. The first app screen is a visual placeholder; app discovery,
launching, media integration, sensors, and the HOME launcher intent will be added incrementally.

## Requirements

- Android SDK with platform `android-37` and Build Tools `36.0.0`
- JDK supported by Android Gradle Plugin 9.4 (JDK 17 is the documented baseline)
- Gradle 9.6.0 (the wrapper will be added during the next setup step)

Your phone can remain on Android 13 (API 33); the app's minimum supported version is API 26.

## Package

`dev.cyberdeck`

## Project structure

- `app/src/main/java/dev/cyberdeck/MainActivity.kt` — activity entry point
- `app/src/main/java/dev/cyberdeck/ui/CyberdeckScreen.kt` — first UI screen
- `app/src/main/java/dev/cyberdeck/ui/theme/Color.kt` — palette
- `app/src/main/res/` — Android resources and manifest
