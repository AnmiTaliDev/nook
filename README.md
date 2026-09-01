# Nook

![License](https://img.shields.io/badge/license-GPLv3-blue.svg)
![Platform](https://img.shields.io/badge/platform-Android-3DDC84.svg)
![Min SDK](https://img.shields.io/badge/minSdk-31-informational.svg)

Nook is a file manager for Android. It is for people who want a file manager
that follows current Material Design guidelines closely without giving up
file management capabilities such as batch operations, archives, and
multiple storage sources.

## Dependencies

- Kotlin, Jetpack Compose, Compose Material 3 (Expressive components)
- Jetpack Navigation 3
- Hilt for dependency injection
- Kotlin Coroutines and Flow
- Kotlin Serialization

Exact versions are pinned in `gradle/libs.versions.toml`.

## Build

Requirements:

- JDK 17
- Android SDK with platform 37 and build-tools 37.0.0 installed
- Android 12 (API 31) or newer for running the app

```sh
./gradlew :app:assembleDebug
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`.
Alternatively, open the project root in Android Studio and run the `app`
configuration.

To run unit tests:

```sh
./gradlew test
```

## Status

Nook is under active early development. See [roadmap.md](roadmap.md) for
planned milestones and their current scope.

## Acknowledgments

Nook builds on the Android Open Source Project, Jetpack Compose, and the
Material Design system maintained by Google.

## Documentation and contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for how to contribute, and
[roadmap.md](roadmap.md) for the development plan.

## License

Nook is licensed under the GNU General Public License v3.0. See
[LICENSE](LICENSE) for the full text.
