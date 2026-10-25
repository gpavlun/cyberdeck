# Cyberdeck

A personal Android launcher inspired by cyberpunk interfaces, retro-futurism, and terminal-based computing.

Built with native Kotlin and Jetpack Compose, Cyberdeck transforms an Android device into a compact, system-oriented interface featuring live telemetry, media controls, application launching, and a built-in terminal.

There is no backend or cloud service. This is a personal project focused on building a functional, customized computing environment.

## Current Status

The core launcher functionality is operational. Development has progressed beyond the initial UI and build skeleton to include working telemetry, application launching, media integration, and terminal interaction.

Current development focuses on visual refinement, configuration options, and restrained interface animations.

## Features

- **System Dashboard** — Displays battery level, Wi-Fi signal strength, cellular signal, Bluetooth state, and location information.
- **Media Controls** — Displays active media session information, track title, artist, playback progress, and previous, play/pause, and next controls. Tapping the media panel attempts to open the source application.
- **Voice Link** — Displays an indicator when an active call is detected through Android's Telecom framework.
- **Application Launcher** — Provides categorized access to installed applications.
- **Quick Access Modules** — Provides shortcuts to system functions and related applications, including communications, camera, networking, audio, logs, and applications.
- **Built-in Terminal** — Provides an interactive command interface for supported operations.
- **Full-Screen Interface** — Uses a custom visual design featuring a dark background, electric-blue accents, angular panel borders, and monospaced typography.

Some functionality depends on Android permissions, system services, and device capabilities. Call detection may not cover VoIP applications that do not use Android's Telecom framework.

## Design Philosophy

Cyberdeck is built around the idea that an interface should feel like an instrument panel for the device it controls.

Rather than replicating a conventional Android home screen, the project aims to create a cohesive, functional environment inspired by cyberpunk field terminals and retro-futuristic computing.

Functionality takes priority over appearance, with visual design reinforcing the purpose of each component. Customization and animation are intended to enhance the experience without compromising usability.

## Requirements

- Android SDK with platform `android-37` and Build Tools `36.0.0`
- JDK supported by Android Gradle Plugin 9.4; JDK 17 is the documented baseline
- Gradle 9.6.0, using the project wrapper when available
- Android device running API 26 or later

The app's minimum supported Android version is API 26. A device running Android 13 (API 33) can run the application.

## Building and Deployment

From the project root, run:

```bash
./build.sh && ./upload.sh
```

The build script compiles the application, and the upload script handles deployment according to the local project configuration.

## Package

`dev.cyberdeck`

## Project Structure

```text
app/src/main/
├── java/dev/cyberdeck/
│   ├── MainActivity.kt
│   └── ui/
│       ├── Main.kt
│       └── theme/
│           └── Color.kt
└── res/
```

- `MainActivity.kt` — Activity entry point and Android system integration.
- `ui/Main.kt` — Main launcher interface and UI components.
- `ui/theme/Color.kt` — Color palette definitions.
- `res/` — Android resources and manifest.

## License

No license has been specified. Unless a license is added to the repository, all rights remain with the copyright holder.
