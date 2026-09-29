# PublivoreTube

PublivoreTube is an experimental, open-source Android TV client project focused on a clean, remote-first viewing experience and modular playback/content providers.

## Project goals

- First-class Android TV / 10-foot UI.
- D-pad and remote friendly navigation.
- Modular video metadata and playback providers.
- Media3-based playback.
- Optional ad/sponsor avoidance features implemented as separate modules.
- Local settings and privacy controls.
- No bundled Google/YouTube proprietary assets.

## Current status

**v0.1.0 — foundation**

The repository currently contains:

- Android TV application shell.
- Jetpack Compose for TV UI.
- Demo home feed.
- Media3 / ExoPlayer playback foundation.
- Repository and player abstractions for future providers.
- GitHub Actions build workflow.

The YouTube content provider and playback implementation are intentionally not part of this first scaffold.

## Build

Open the project in Android Studio with JDK 17+ and sync the Gradle project.

For a machine with a compatible Gradle installation:

```bash
gradle :app:assembleDebug
```

Install the resulting APK on an Android TV device/emulator with:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Architecture

```text
UI (Compose for TV)
        │
        ▼
Presentation / State
        │
   ┌────┴────┐
   ▼         ▼
Content     Player
Provider    Manager
   │         │
   ▼         ▼
Metadata    Media3
```

## Roadmap

### 0.1
- [x] Android TV shell
- [x] D-pad friendly home UI
- [x] Media3 dependency
- [x] Repository abstraction
- [ ] Real thumbnail rendering
- [ ] Player screen

### 0.2
- [ ] Search UI
- [ ] Channel pages
- [ ] Watch history
- [ ] Settings
- [ ] Real content provider

### 0.3
- [ ] Sponsor/segment skipping module
- [ ] Playback filtering module
- [ ] Better player controls
- [ ] Persistent preferences

## Development principles

PublivoreTube should keep content retrieval, playback, filtering, and UI independent so individual components can be replaced without rewriting the application.

## License

MIT — see [LICENSE](LICENSE).
