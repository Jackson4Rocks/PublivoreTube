# PublivoreTube

PublivoreTube is an experimental, open-source Android TV client focused on a polished 10-foot experience, D-pad navigation, modular content providers, and a future ad/sponsor-aware playback stack.

## What is implemented

- Android TV / Leanback launcher support.
- Android 8.0 (API 26) and newer.
- Google TV Material 3 components through `androidx.tv:tv-material`.
- Remote-first focus behavior with predictable D-pad navigation.
- Dedicated Search, About, History, Settings, and Subscriptions destinations.
- Generated PublivoreTube purple-gradient P app icon.
- YouTube Data API v3 integration for:
  - Most-popular video feed.
  - YouTube video search.
  - Video thumbnails.
  - Video durations.
- Demo-feed fallback when an API key is not configured.
- Media3 / ExoPlayer foundation for the future native playback layer.
- GitHub Actions debug APK builds with downloadable APK artifacts.

## YouTube Data API setup

PublivoreTube deliberately does **not** store a Google API key in the repository.

1. Create or select a Google Cloud project.
2. Enable the **YouTube Data API v3**.
3. Create an API key and restrict it appropriately for the application.
4. In a local checkout, add this to `local.properties`:

```properties
YOUTUBE_API_KEY=YOUR_API_KEY_HERE
```

The Gradle build also accepts a `YOUTUBE_API_KEY` environment variable, which is useful for CI or private build machines.

Without a key, the app still launches and uses the built-in demo feed so the UI can be tested without credentials.

## Important playback note

The YouTube Data API provides metadata and search results; it does **not** provide a general-purpose direct video stream URL. PublivoreTube therefore keeps metadata retrieval separate from playback.

Selecting a video currently opens its normal YouTube URL as a compatibility fallback. The planned native Media3 playback layer will be developed separately rather than pretending the Data API itself provides playable media streams.

## Build

Open the project in Android Studio with JDK 17+ and sync Gradle.

```bash
gradle :app:assembleDebug
```

Install the resulting APK:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Project structure

```text
UI / Compose for TV
        │
        ▼
VideoRepository
   ┌────┴───────────┐
   ▼                ▼
YouTube Data API   Demo fallback
        │
        ▼
      Video
        │
        ▼
Future Media3 playback layer
```

## Maintainer

**Leon Sony** — project creator and maintainer.

GitHub: https://github.com/Jackson4Rocks

## Roadmap

### v0.1.x
- [x] Android TV shell
- [x] Remote-first Material 3 UI
- [x] YouTube Data API metadata/search
- [x] Real YouTube thumbnails
- [x] About / maintainer page
- [x] Custom PublivoreTube icon
- [x] CI APK artifact

### v0.2
- [ ] Native in-app player
- [ ] Video details page
- [ ] Watch history
- [ ] Persistent settings
- [ ] Channel pages
- [ ] Better search filters

### v0.3
- [ ] Sponsor/segment handling
- [ ] Playback filtering
- [ ] Improved playback controls
- [ ] Account-aware features

## License

MIT — see [LICENSE](LICENSE).
