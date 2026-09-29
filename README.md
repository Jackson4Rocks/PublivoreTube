<div align="center">

# 📺 PublivoreTube

### A polished, remote-first YouTube client for Android TV

[![Android Build](https://github.com/Jackson4Rocks/PublivoreTube/actions/workflows/android.yml/badge.svg)](https://github.com/Jackson4Rocks/PublivoreTube/actions/workflows/android.yml)
[![Platform](https://img.shields.io/badge/platform-Android%20TV-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/tv)
[![Min SDK](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/about/versions/oreo)
[![License](https://img.shields.io/badge/license-MIT-8B5CF6?style=for-the-badge)](LICENSE)

<br>

```text
     _            _   _  _         ____                      
    | | ___  ___ |  | || |__     / ___|  ___  _ __  _   _ 
 _  | |/ _ \/ _ \|  \| ||  _ \   \___ \ / _ \| '_ \| | | |
| |_| |  __/ (_) | |\  || | | |   ___) | (_) | | | | |_| |
 \___/ \___|\___/|_| \_||_| |_|  |____/ \___/|_| |_|\__, |
                                                    |___/

                    Jackson4Rocks
                       Leon Sony
```

<br>

**PublivoreTube** is an experimental open-source Android TV client built around a proper 10-foot interface: large content cards, predictable D-pad focus, Material 3 styling, and a clean separation between metadata and playback.

</div>

---

## ✨ Highlights

- 📺 **Android TV first** — Leanback launcher support and remote-friendly navigation.
- 🎮 **D-pad focused UI** — predictable focus movement and visible focus states.
- 🎨 **Material 3 for TV** — built with `androidx.tv:tv-material`.
- 🔎 **YouTube Data API v3** — popular videos, search, thumbnails, durations, and metadata.
- 🖼️ **Real thumbnails** — content cards use YouTube thumbnail URLs when available.
- 🧪 **Demo fallback** — the UI still works without an API key.
- ▶️ **Media3 foundation** — playback architecture is separated from metadata retrieval.
- ⚡ **CI builds** — GitHub Actions produces downloadable Android APK artifacts.
- 🔐 **Local credentials** — API keys and signing credentials stay out of the repository.

## 🖥️ UI stack

```text
┌──────────────────────────────────────────────────────────┐
│ PublivoreTube                                             │
│                                                          │
│  Home   Search   Subscriptions   History   Settings     │
│                                                          │
│  ┌────────────────────────────────────────────────────┐  │
│  │                  Featured video                    │  │
│  └────────────────────────────────────────────────────┘  │
│                                                          │
│  Recommended                                             │
│  [ Thumbnail ] [ Thumbnail ] [ Thumbnail ] [ Thumbnail ]│
│                                                          │
│  Trending                                                │
│  [ Thumbnail ] [ Thumbnail ] [ Thumbnail ] [ Thumbnail ]│
└──────────────────────────────────────────────────────────┘
```

The app is designed for a TV remote rather than a touch-first phone layout.

## 📦 Current features

| Feature | Status |
|---|:---:|
| Android TV / Leanback launcher | ✅ |
| Android 8.0+ | ✅ |
| Material 3 for TV | ✅ |
| D-pad navigation | ✅ |
| Home / Search / Subscriptions / History / Settings / About | ✅ |
| YouTube Data API metadata | ✅ |
| YouTube search | ✅ |
| Real thumbnails | ✅ |
| Demo feed | ✅ |
| Media3 / ExoPlayer foundation | ✅ |
| Native in-app YouTube playback | 🚧 |

## 🔑 YouTube Data API

PublivoreTube does **not** commit a Google API key to GitHub.

Create a local `local.properties` file:

```properties
YOUTUBE_API_KEY=YOUR_API_KEY_HERE
```

The Gradle build also accepts:

```bash
export YOUTUBE_API_KEY="YOUR_API_KEY_HERE"
```

Without a key, the app falls back to the built-in demo feed.

### Important

The YouTube Data API provides video metadata and search results; it does not provide a general-purpose direct playback URL. PublivoreTube therefore keeps **metadata retrieval** separate from the **playback layer**.

For the current build, selecting a video opens its normal YouTube URL as a compatibility fallback.

## 🛠️ Build locally

Requirements:

- JDK 17+
- Android SDK
- Android SDK Platform 36
- Android Build Tools 36.0.0
- Gradle 9.6.1 or the project's configured Gradle version

Debug build:

```bash
gradle :app:assembleDebug
```

Release build:

```bash
gradle :app:assembleRelease
```

Install a debug APK:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Release output:

```text
app/build/outputs/apk/release/app-release.apk
```

### 🔐 Release signing

Keep these files local:

```text
local.properties
*.jks
*.keystore
```

The repository intentionally does not contain release passwords or signing keys.

## 🏗️ Architecture

```text
                    ┌─────────────────────┐
                    │  Compose for TV UI  │
                    └──────────┬──────────┘
                               │
                               ▼
                    ┌─────────────────────┐
                    │  VideoRepository    │
                    └──────────┬──────────┘
                           ┌───┴───┐
                           ▼       ▼
                  ┌────────────┐  ┌─────────────┐
                  │ YouTube    │  │ Demo Feed   │
                  │ Data API   │  │ Fallback    │
                  └─────┬──────┘  └─────────────┘
                        │
                        ▼
                  ┌─────────────┐
                  │    Video    │
                  └──────┬──────┘
                         │
                         ▼
                  ┌─────────────┐
                  │   Media3    │
                  │  Playback   │
                  └─────────────┘
```

## 👾 Pac-Man contribution graph

The repository also generates a Pac-Man version of the GitHub contribution grid with GitHub Actions. The generated SVG is published to the `output` branch and embedded below.

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/Jackson4Rocks/PublivoreTube/output/pacman-contribution-graph-dark.svg">
  <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/Jackson4Rocks/PublivoreTube/output/pacman-contribution-graph.svg">
  <img alt="Pac-Man contribution graph" src="https://raw.githubusercontent.com/Jackson4Rocks/PublivoreTube/output/pacman-contribution-graph.svg">
</picture>

Generated with [abozanona/pacman-contribution-graph](https://github.com/abozanona/pacman-contribution-graph).

## 🚀 Roadmap

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
- [ ] Persistent watch history
- [ ] Persistent settings
- [ ] Channel pages
- [ ] Better search filters

### v0.3
- [ ] Sponsor/segment handling
- [ ] Playback filtering
- [ ] Improved playback controls
- [ ] Account-aware features

## 👤 Maintainer

**Leon Sony**  
Project creator and maintainer.

GitHub: **[@Jackson4Rocks](https://github.com/Jackson4Rocks)**

## 📄 License

MIT — see [LICENSE](LICENSE).

---

<div align="center">

**Built for the couch. Powered by open source.** 📺💜

</div>
