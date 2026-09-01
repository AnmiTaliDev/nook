# Contributing to Nook

Thanks for your interest in contributing to Nook.

## Reporting issues

Open an issue with:

- Android version and device model (or emulator configuration).
- Steps to reproduce.
- Expected vs. actual behavior.
- Logs or screenshots if relevant.

## Development setup

See the Build section in [README.md](README.md) for toolchain requirements.
The project is a standard multi-module Gradle/Android project and opens
directly in Android Studio.

## Code style

- Kotlin, following the official Kotlin coding conventions.
- Prefer Jetpack Compose idioms; avoid introducing View-based UI unless
  there is no Compose equivalent.
- Keep UI, domain, and data layers separated along existing module
  boundaries. New file sources should implement the `FileSource` interface
  in `core:domain` rather than reaching into `java.io.File` from feature
  modules.
- All user-facing strings go into `strings.xml`. Do not hardcode text in
  Composables.
- Run `./gradlew test` before submitting a pull request. Add or update
  tests for behavior changes.

## Commit messages

Use [Conventional Commits](https://www.conventionalcommits.org/) format,
e.g. `feat: add archive extraction`, `fix: correct breadcrumb navigation`.
Keep commits atomic: one logical change per commit.

## Pull requests

- Base your branch on `main`.
- Keep pull requests focused; unrelated changes should be separate PRs.
- Describe what changed and why in the PR description.
- Make sure the project builds (`./gradlew :app:assembleDebug`) and tests
  pass before requesting review.

## License

By contributing, you agree that your contributions will be licensed under
the GNU General Public License v3.0, the same license that covers the rest
of the project.
