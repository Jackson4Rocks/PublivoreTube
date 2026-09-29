<div align="center">

<img src="https://raw.githubusercontent.com/Jackson4Rocks/PublivoreTube/main/docs/assets/material3-expressive-banner.svg" alt="PublivoreTube Material 3 Expressive banner" width="100%"/>

### 📺 YouTube, reimagined for your TV.

A polished, open-source Android TV client focused on **remote-first navigation**, **Material 3 Expressive-inspired UI**, and a clean separation between **content metadata** and **playback**.

[![Android Build](https://github.com/Jackson4Rocks/PublivoreTube/actions/workflows/android.yml/badge.svg)](https://github.com/Jackson4Rocks/PublivoreTube/actions/workflows/android.yml)
[![Android TV](https://img.shields.io/badge/Android%20TV-8.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/tv)
[![Kotlin](https://img.shields.io/badge/Kotlin-Compose-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/MIT-License-4F378B?style=for-the-badge)](LICENSE)

</div>

---

## ✦ What is PublivoreTube?

PublivoreTube is my Android TV client project built around one idea:

> **The TV experience should feel like a TV experience.**

That means big visual targets, predictable D-pad movement, obvious focus states, comfortable spacing, and a UI that feels expressive without becoming noisy.

The project is intentionally modular so the **YouTube Data API** handles public metadata/search while the playback layer can evolve independently.

<table>
<tr>
<td width="50%">

### ◈ Built for the couch

- D-pad / remote-first navigation
- Android TV launcher integration
- Large cards and readable typography
- Dedicated Home, Search, Subscriptions, History, Settings and About areas
- Android 8.0+ target

</td>
<td width="50%">

### ◆ Built to experiment

- Jetpack Compose
- Compose for TV / Material 3 for TV
- YouTube Data API v3
- Coil thumbnail loading
- Media3 / ExoPlayer foundation
- Demo feed when no API key is configured

</td>
</tr>
</table>

---

## ✦ Material 3 Expressive direction

PublivoreTube's visual language takes inspiration from the newer **Material 3 Expressive** direction: stronger shape contrast, playful geometry, clear hierarchy, and surfaces that make focus feel intentional.

<div align="center">

| 🟣 **Pill** | 🟪 **Squircle** | ◆ **Diamond** | 🟢 **Circle** | 🔶 **Asymmetric form** |
|:---:|:---:|:---:|:---:|:---:|
| Navigation chips | App / content containers | Focus accents | Status & actions | Hero decoration |

</div>

The README banner above uses the same visual vocabulary: **rounded containers, pill controls, diamonds, blobs, circles, strong tonal layers, and playful asymmetric shapes** against a dark TV-oriented surface.

---

## ✦ Feature set

<table>
<tr>
<td align="center" width="33%">

### 📺 TV-native
Leanback launcher support, 10-foot layouts, and predictable remote navigation.

</td>
<td align="center" width="33%">

### 🔎 YouTube data
Popular videos, search, thumbnails, durations, and public metadata through YouTube Data API v3.

</td>
<td align="center" width="33%">

### 🎨 Expressive UI
Large cards, focus states, tonal surfaces, rounded shapes, and a TV-first information hierarchy.

</td>
</tr>
<tr>
<td align="center">

### 🧩 Modular
Repository-based data access keeps UI, metadata, and playback responsibilities separated.

</td>
<td align="center">

### 🛟 Demo fallback
The app can still launch and showcase the UI without a configured API key.

</td>
<td align="center">

### ⚙️ Media3 foundation
Playback architecture is prepared for future native playback work.

</td>
</tr>
</table>

---

## ✦ Current state

| Area | Status |
|---|:---:|
| Android TV / Leanback launcher | ✅ |
| Android 8.0+ | ✅ |
| D-pad navigation | ✅ |
| Material 3 for TV | ✅ |
| Material 3 Expressive visual direction | 🚧 |
| YouTube Data API metadata | ✅ |
| YouTube search | ✅ |
| Real thumbnails | ✅ |
| Demo feed | ✅ |
| Media3 / ExoPlayer foundation | ✅ |
| Native in-app video playback | 🚧 |
| Persistent history | 🚧 |
| Persistent settings | 🚧 |

---

## ✦ Architecture

```text
                 ┌───────────────────────────┐
                 │     Compose / TV UI      │
                 │  expressive TV surfaces  │
                 └─────────────┬─────────────┘
                               │
                               ▼
                 ┌───────────────────────────┐
                 │      VideoRepository     │
                 └─────────────┬─────────────┘
                         ┌─────┴─────┐
                         ▼           ▼
                ┌──────────────┐  ┌─────────────┐
                │ YouTube API  │  │ Demo Feed   │
                │   v3         │  │  fallback   │
                └──────┬───────┘  └─────────────┘
                       │
                       ▼
                ┌──────────────┐
                │    Video     │
                └──────┬───────┘
                       │
                       ▼
                ┌──────────────┐
                │    Media3    │
                │   playback   │
                └──────────────┘
```

---

## ✦ YouTube Data API

PublivoreTube **does not commit API credentials** to the repository.

Create a local `local.properties`:

```properties
YOUTUBE_API_KEY=YOUR_API_KEY_HERE
```

A `YOUTUBE_API_KEY` environment variable is also supported.

Without a key, the app falls back to its built-in demo content.

### Playback note

The YouTube Data API provides **metadata and search results**, not a general-purpose direct media stream URL.

For the current implementation, selecting a video opens its normal YouTube URL as a compatibility fallback. Native in-app playback remains a separate development track.

---

## ✦ Build

### Requirements

- JDK 17+
- Android SDK
- Android SDK Platform 36
- Android Build Tools 36.0.0
- A Gradle version compatible with the project

### Debug

```bash
gradle :app:assembleDebug
```

### Release

```bash
gradle :app:assembleRelease
```

### Install over ADB

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Release output:

```text
app/build/outputs/apk/release/app-release.apk
```

---

## ✦ Release signing

The release build uses a local signing keystore.

Keep these **out of GitHub**:

```text
local.properties
*.jks
*.keystore
```

The repository contains only the configuration needed to read local signing values. The actual keystore and passwords remain on your machine / CI secrets.

---

## ✦ Pac-Man corner 👾

The repository includes a GitHub Actions workflow for generating a Pac-Man-style contribution graph.

The generated graphic is published to the `output` branch so it can be embedded back into the README.

<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://raw.githubusercontent.com/Jackson4Rocks/PublivoreTube/output/pacman-contribution-graph-dark.svg">
  <source media="(prefers-color-scheme: light)" srcset="https://raw.githubusercontent.com/Jackson4Rocks/PublivoreTube/output/pacman-contribution-graph.svg">
  <img alt="Pac-Man contribution graph" src="https://raw.githubusercontent.com/Jackson4Rocks/PublivoreTube/output/pacman-contribution-graph.svg" width="100%">
</picture>

---

## ✦ Roadmap

### 0.1.x — Foundation
- [x] Android TV shell
- [x] Remote-first UI
- [x] Material 3 TV components
- [x] YouTube Data API metadata/search
- [x] Real thumbnails
- [x] About / maintainer page
- [x] Custom app icon
- [x] CI APK artifacts

### 0.2 — Experience
- [ ] Native in-app player
- [ ] Video details screen
- [ ] Persistent history
- [ ] Persistent settings
- [ ] Channel pages
- [ ] Search filters

### 0.3 — Power features
- [ ] Playback controls
- [ ] Sponsor / segment handling
- [ ] Playback filtering
- [ ] Account-aware features

---

## ✦ Maintainer

<div align="center">

### Leon Sony

**Project creator & maintainer**

[![GitHub](https://img.shields.io/badge/GitHub-Jackson4Rocks-1D1B20?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Jackson4Rocks)

Built in public.  
**Build • Break • Improve • Repeat.**

</div>

---

<div align="center">

### 💜 PublivoreTube

**Open source. TV-first. Still evolving.**

</div>
