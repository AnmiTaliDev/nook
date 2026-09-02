# Nook Roadmap

Status legend: `[x]` done, `[ ]` not started yet. Versions before 1.0 are
pre-release milestones; each one is expected to be usable on its own, not a
throwaway increment.

## v0.1 — Foundation and basic browsing

- [x] Multi-module Gradle setup: `app`, `core:model`, `core:common`,
      `core:navigation`, `core:designsystem`, `core:domain`, `core:data`,
      `feature:browse`.
- [x] Hilt dependency graph wired end to end (`NookApplication`, `FileSource`
      binding, dispatcher provider).
- [x] `FileSource` abstraction in `core:domain` with a single
      `LocalFileSource` implementation in `core:data`, so no call site
      depends on `java.io.File` directly.
- [x] Jetpack Navigation 3 host in `MainActivity` (`NavDisplay`,
      `NavBackStack`, `entryProvider`).
- [x] Material 3 Expressive design system: full shape scale (extra-small to
      extra-large), full type scale (display/headline/title/body/label ×
      small/medium/large), `MotionScheme.expressive()`, static light/dark
      tonal palettes, dynamic color support (Android 12+).
- [x] Directory browsing screen: list current directory contents, open
      subdirectories, breadcrumb navigation, up-navigation via system back.
- [x] `MANAGE_EXTERNAL_STORAGE` permission flow with a dedicated in-app
      state (not a silent failure) when access has not been granted.
- [x] Predictive back enabled at the manifest level
      (`enableOnBackInvokedCallback`).
- [x] English strings only, all interactive elements carry
      `contentDescription`, minimum 48dp touch targets.
- [x] Unit tests for `core:domain` use cases with fakes.

## v0.2 — File operations

- [x] Copy, move, rename, delete for files and directories.
- [x] Conflict resolution UI when a destination name already exists:
      overwrite, skip, or keep both (auto-renamed), applied per batch.
- [x] Multi-select mode in the file list (long-press to enter, tap to
      toggle) with batch copy/cut/paste/delete.
- [x] Progress reporting for long-running operations (determinate progress,
      cancel action).
- [ ] Long-running operations backed by a foreground service or
      `WorkManager` so they survive process death (currently scoped to
      `viewModelScope`, lost if the process is killed mid-operation).
- [x] Undo action (snackbar) for delete: deleted items move to a
      `.nook_trash` holding directory and can be restored.
- [x] Create new file / new folder actions.
- [ ] Internal action-registry shape for file operations (registry is only
      exposed publicly in v0.9, but operations are implemented behind an
      interface from the start so v0.9 does not require a rewrite).

## v0.3 — Search, sort, and grouping

- [x] Recursive filename search scoped to the current directory, with a
      cancellable in-progress indicator. Whole-volume search (from the
      drawer, without first navigating to the volume root) is not wired up
      yet.
- [x] Search result list reuses the file row component from
      `feature:browse`.
- [x] Sorting by name, date modified, size, and type, ascending/descending.
- [x] Grouping by type (images, documents, archives, etc.) and by date
      (today, yesterday, this week, older).
- [ ] Sort/group preference persisted per directory and globally (user can
      pin a default). Currently resets to name/ascending/no-grouping on
      each process start.

## v0.4 — Bookmarks, file info, and volumes

- [x] Bookmark/favorite any directory; bookmarks shown in a dedicated
      section of the navigation drawer, persisted via SharedPreferences +
      kotlinx.serialization.
- [x] File/folder info sheet: full path, size (recursive for folders,
      computed asynchronously), created/modified dates, MIME type, POSIX
      permissions, owner where available.
- [x] Volume switcher: internal storage and removable SD card (when
      present) listed with free/used space, exposed through
      `GetVolumesUseCase`.
- [x] Empty states and low-storage warnings (banner shown when the current
      volume has under 10% free space).

## v0.5 — Archives

- [x] `feature:archive` module: create `.zip` archives from a selection
      (via zip4j), extract `.zip` archives into a chosen directory. Tapping
      a `.zip`/`.tar`/`.tar.gz`/`.7z` file opens a dedicated archive-contents
      screen (new `ArchiveRoute` Navigation 3 destination).
- [x] Read-only support for browsing `.tar`, `.tar.gz`, and `.7z` contents
      without full extraction, via Apache Commons Compress.
- [ ] Archive operations registered through the action registry introduced
      in v0.2/v0.9, not hardcoded into `feature:browse`. Still deferred: all
      operations (including archives) are wired directly into
      `BrowseViewModel`, same as copy/move/delete.
- [x] Password-protected zip extraction (prompt for password, surface wrong
      password as a distinct error state via `WrongPasswordException`).

## v0.6 — Accessibility and localization pass

- [x] Static TalkBack audit of every `contentDescription` across
      `feature:browse` and `feature:archive`: confirmed decorative icons
      next to visible labels are correctly `null`, and fixed a real gap —
      multi-select rows had no accessible "selected" state (checkmark icons
      were decorative-only); added `Modifier.semantics { selected = ... }`.
      Not yet verified with a real TalkBack pass on-device (no emulator
      available in this environment).
- [x] WCAG AA contrast verification for the static light/dark themes:
      computed relative-luminance contrast ratios for every on-X/X color
      pair, all pass 4.5:1 (exceeding the 3:1 large-text/UI minimum).
      Dynamic-color contrast is inherently device/wallpaper-dependent and
      not spot-checked here.
- [ ] RTL layout testing using a pseudo-RTL locale. Code-level audit done
      (no hardcoded left/right padding found; fixed one non-mirrored icon,
      `Icons.Filled.Sort` → `Icons.AutoMirrored.Filled.Sort`), but no actual
      on-device/emulator RTL render check performed.
- [ ] Large font scale (up to 200%) testing across all screens. Code-level
      audit done (no fixed `.height()`/`.size()` constraints on
      text-bearing containers found), but no on-device render check
      performed.
- [x] First additional language beyond English: Russian
      (`values-ru/strings.xml`) added for `feature:browse` and
      `feature:archive`, with 1:1 key parity verified against the English
      base.
- [x] Switch/keyboard navigation: audited for accidental loss of default
      Compose focusability/semantics (none found — all interactive elements
      use standard `clickable`/`combinedClickable`/button components, which
      are focusable and Enter/D-pad activatable by default). Not verified
      with a physical keyboard or switch device.

## v0.7 — Adaptive layouts

- [ ] `WindowSizeClass`-driven layout switch: single-pane on compact width,
      list-detail two-pane on medium/expanded width (tablets, unfolded
      foldables).
- [ ] Navigation rail replaces bottom/drawer navigation on larger widths.
- [ ] Multi-window and split-screen behavior verified on phones and
      tablets.
- [ ] Foldable-specific testing: fold/unfold transition, hinge-aware layout
      via `WindowLayoutInfo` where applicable.

## v0.8 — Pluggable file preview

- [ ] `core:viewer` module: `FilePreviewProvider` interface plus a registry
      keyed by MIME type/extension, so preview types are additive.
- [ ] Built-in providers: plain text/code (with basic syntax highlighting),
      images (pan/zoom), and a fallback "open with" action for unsupported
      types.
- [ ] Preview opens as a distinct Navigation 3 destination (shared-element
      transition from the file row's thumbnail/icon).
- [ ] Thumbnail generation and caching for images.

## v0.9 — Public action registry and custom actions

- [ ] `core:actions` promoted from an internal detail to a documented,
      extensible surface: `FileAction` interface, `ActionRegistry` with
      Hilt multibinding, context-sensitive availability (e.g. "Extract
      here" only for archives).
- [ ] All built-in operations (copy/move/delete/rename/archive) migrated to
      be registered actions rather than hardcoded menu entries.
- [ ] Per-file-type contextual action menus driven entirely by the
      registry.

## v1.0 — Stabilization and public release

- [ ] Full unit test coverage for ViewModels and domain use cases; Compose
      UI tests for browsing, multi-select, file operations, and search.
- [ ] Performance pass on directories with 10k+ entries
      (paging/virtualization in the file list, background sorting).
- [ ] Full accessibility and localization re-audit.
- [ ] Design QA pass across all Expressive components in both themes and
      all supported window size classes.
- [ ] Crash reporting/analytics opt-in (privacy-respecting, off by
      default).
- [ ] First tagged public release.

## Post-1.0 — Long-term goals

- [ ] **Encrypted "secret" directory**: PIN/biometric-gated storage area
      backed by strong, current encryption (implementation choice — e.g.
      AES-256 via Android Keystore — to be finalized against best practice
      at implementation time), exposed through a new `FileSource`
      implementation so `feature:browse` and other features require no
      changes to consume it; files not reachable through normal OS file
      access.
- [ ] **Cloud sources**: first third-party storage provider integrated
      behind the existing `FileSource` interface; account management UI;
      offline caching strategy.
- [ ] **Cross-device sync**: consistent bookmarks/settings across a user's
      devices.
- [ ] **Advanced archive support**: writing `.7z`/`.tar.gz`, browsing
      nested archives, RAR extraction where licensing allows.
- [ ] **Root/advanced storage access** (optional, clearly separated from
      the default sandboxed experience) for power users who need it.
- [ ] **Tag-based file organization** as an alternative to strict folder
      hierarchies.
